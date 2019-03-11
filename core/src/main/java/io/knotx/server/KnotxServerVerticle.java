/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.server;

import io.knotx.server.api.handler.RoutingHandlerFactory;
import io.knotx.server.api.security.AuthHandlerFactory;
import io.knotx.server.configuration.AuthHandlerOptions;
import io.knotx.server.configuration.KnotxServerOptions;
import io.knotx.server.configuration.RoutingOperationOptions;
import io.knotx.server.exceptions.ConfigurationException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.SingleSource;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KnotxServerVerticle extends AbstractVerticle {

  public static final String KNOTX_PORT_PROP_NAME = "knotx.port";
  public static final String KNOTX_FILE_UPLOAD_DIR_PROPERTY = "knotx.fileUploadDir";

  private static final Logger LOGGER = LoggerFactory.getLogger(KnotxServerVerticle.class);

  private static final HttpResponseStatus BAD_REQUEST = HttpResponseStatus.BAD_REQUEST;

  private KnotxServerOptions options;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    options = new KnotxServerOptions(config());
  }

  @Override
  public void start(Future<Void> fut) {
    LOGGER.info("Starting <{}>", this.getClass().getSimpleName());
    LOGGER.info("Open API specification location [{}]",
        options.getRoutingSpecificationLocation());
    validateRoutingOperations();

    OpenAPI3RouterFactory.rxCreate(vertx, options.getRoutingSpecificationLocation())
        .doOnSuccess(this::configureSecurity)
        .doOnSuccess(this::configureRouting)
        .doOnSuccess(OpenAPI3RouterFactory::mountServicesFromExtensions)
        .map(OpenAPI3RouterFactory::getRouter)
        .doOnSuccess(this::logRouterRoutes)
        .flatMap(this::configureHttpServer)
        .subscribe(
            ok -> {
              LOGGER.info("Knot.x HTTP Server started. Listening on port {}",
                  options.getServerOptions().getPort());
              fut.complete();
            },
            error -> {
              LOGGER.error("Unable to start Knot.x HTTP Server.", error.getCause());
              fut.fail(error);
            }
        );
  }

  private void configureSecurity(OpenAPI3RouterFactory routerFactory) {
    final Map<String, AuthHandlerFactory> authHandlerFactoriesByName = loadAuthHandlerFactories();
    options.getSecurityHandlers().forEach(options -> {
      registerAuthHandler(routerFactory,
          authHandlerFactoriesByName.get(options.getFactory()), options);
      LOGGER.info("Security handler [{}] initialized", options.getSchema());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Auth handler initialization details [{}]", options.toJson().encodePrettily());
      }
    });
  }

  private Map<String, AuthHandlerFactory> loadAuthHandlerFactories() {
    List<AuthHandlerFactory> routingFactories = new ArrayList<>();
    ServiceLoader.load(AuthHandlerFactory.class)
        .iterator()
        .forEachRemaining(routingFactories::add);

    LOGGER.info("Auth handler factory types registered: " +
        routingFactories.stream().map(AuthHandlerFactory::getName).collect(Collectors
            .joining(",")));

    return routingFactories.stream()
        .collect(Collectors.toMap(AuthHandlerFactory::getName, Function.identity()));
  }

  private void registerAuthHandler(OpenAPI3RouterFactory routerFactory,
      AuthHandlerFactory authHandlerFactory, AuthHandlerOptions options) {
    if (routerFactory != null) {
      routerFactory.addSecurityHandler(options.getSchema(),
          authHandlerFactory.create(vertx, options.getConfig()));
    } else {
      throw new ConfigurationException("Factory for " + options + " is not registered!");
    }
  }

  private void configureRouting(OpenAPI3RouterFactory routerFactory) {
    List<RoutingHandlerFactory> handlerFactories = loadRoutingHandlerFactories();
    options.getRoutingOperations().forEach(operation -> {
      registerRoutingHandlersPerOperation(routerFactory, handlerFactories, operation);
      registerFailureHandlersPerOperation(routerFactory, handlerFactories, operation);
      LOGGER.info("Initialized all handlers for operation [{}]", operation.getOperationId());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Operation initialization details [{}]", operation.toJson().encodePrettily());
      }
    });
  }

  private void validateRoutingOperations() {
    if (options.getRoutingOperations() == null || options.getRoutingOperations().isEmpty()) {
      LOGGER.warn(
          "The server configuration does not contain any operation defined. Please check your "
              + "configuration [config.server.options.config.routingOperations]");
    }
  }

  private void registerRoutingHandlersPerOperation(OpenAPI3RouterFactory routerFactory,
      List<RoutingHandlerFactory> handlerFactories,
      RoutingOperationOptions operation) {
    operation.getHandlers().forEach(routingHandlerOptions ->
        handlerFactories.stream()
            .filter(
                handlerFactory -> handlerFactory.getName()
                    .equals(routingHandlerOptions.getName()))
            .findAny()
            .map(handlerFactory ->
                routerFactory
                    .addHandlerByOperationId(operation.getOperationId(),
                        handlerFactory.create(vertx, routingHandlerOptions.getConfig()))
            )
            .orElseThrow(() -> {
              LOGGER.error(
                  "Handler factory [{}] not found in registered factories [{}], all options [{}]",
                  routingHandlerOptions.getName(), handlerFactories, operation);
              return new IllegalStateException(
                  "Can not find handler factory for [" + routingHandlerOptions.getName() + "]");
            })
    );
  }

  private void registerFailureHandlersPerOperation(OpenAPI3RouterFactory openAPI3RouterFactory,
      List<RoutingHandlerFactory> handlerFactories,
      RoutingOperationOptions routingOperationOptions) {
    routingOperationOptions.getFailureHandlers().forEach(routingHandlerOptions ->
        handlerFactories.stream()
            .filter(
                handlerFactory -> handlerFactory.getName()
                    .equals(routingHandlerOptions.getName()))
            .findAny()
            .map(handlerFactory ->
                openAPI3RouterFactory
                    .addFailureHandlerByOperationId(routingOperationOptions.getOperationId(),
                        handlerFactory.create(vertx, routingHandlerOptions.getConfig()))
            )
            .orElseThrow(IllegalStateException::new)
    );
  }

  private SingleSource<? extends HttpServer> configureHttpServer(Router router) {
    HttpServer httpServer = vertx.createHttpServer(options.getServerOptions());

    if (options.isDropRequests()) {
      httpServer.requestStream().toFlowable()
          .map(HttpServerRequest::pause)
          .onBackpressureBuffer(options.getBackpressureBufferCapacity(),
              () -> LOGGER.warn("Backpressure buffer is overflown. Dropping request"),
              options.getBackpressureStrategy())
          .onBackpressureDrop(
              req -> req.response().setStatusCode(options.getDropRequestResponseCode()).end())
          .observeOn(RxHelper.scheduler(vertx.getDelegate()))
          .subscribe(req -> {
            req.resume();
            routeSafe(req, router);
          }, error -> LOGGER.error("Exception while processing!", error));
    } else {
      httpServer
          .requestHandler(req -> routeSafe(req, router));
    }

    return httpServer.rxListen();
  }

  private void routeSafe(HttpServerRequest req, Router router) {
    try {
      router.accept(req);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("Problem decoding Query String ", ex);

      req.response()
          .setStatusCode(BAD_REQUEST.code())
          .setStatusMessage(BAD_REQUEST.reasonPhrase())
          .end("Invalid characters in Query Parameter");
    }
  }

  private List<RoutingHandlerFactory> loadRoutingHandlerFactories() {
    List<RoutingHandlerFactory> routingFactories = new ArrayList<>();
    ServiceLoader.load(RoutingHandlerFactory.class)
        .iterator()
        .forEachRemaining(routingFactories::add);

    LOGGER.info("Routing handler factory names [{}] registered.",
        routingFactories.stream().map(RoutingHandlerFactory::getName).collect(Collectors
            .joining(",")));

    return routingFactories;
  }

  private void logRouterRoutes(Router router) {
    LOGGER.info("Routes [{}]", router.getRoutes());
    printRoutes(router);
  }

  private void printRoutes(Router router) {
    // @formatter:off
    System.out.println(
        "\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    System.out.println(
        "@@                              ROUTER CONFIG                                 @@");
    System.out.println(
        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    router.getRoutes().forEach(route -> System.out.println("@@     " + route.getDelegate()));
    System.out.println(
        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
    // @formatter:on
  }

}
