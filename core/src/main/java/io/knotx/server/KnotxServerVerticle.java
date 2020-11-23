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

import io.knotx.server.configuration.HystrixMetricsOptions;
import io.knotx.server.configuration.KnotxServerOptions;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.HystrixMetricHandler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

public class KnotxServerVerticle extends AbstractVerticle {

  public static final String KNOTX_PORT_PROP_NAME = "knotx.port";
  public static final String KNOTX_FILE_UPLOAD_DIR_PROPERTY = "knotx.fileUploadDir";

  private static final Logger LOGGER = LoggerFactory.getLogger(KnotxServerVerticle.class);

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

    GlobalHandlersProvider globalHandlersProvider = new GlobalHandlersProvider(vertx, options.getGlobalHandlers());
    RoutesProvider routesProvider = new RoutesProvider(vertx, options.getRoutingOperations());
    SecurityProvider securityProvider = new SecurityProvider(vertx, options.getSecurityHandlers());
    HttpServerProvider httpServerProvider = new HttpServerProvider(vertx,
        options.getServerOptions(), options.getDropRequestOptions());

    OpenAPI3RouterFactory.rxCreate(vertx, options.getRoutingSpecificationLocation())
        .doOnSuccess(globalHandlersProvider::configureGlobalHandlers)
        .doOnSuccess(securityProvider::configureSecurity)
        .doOnSuccess(routesProvider::configureRouting)
        .doOnSuccess(OpenAPI3RouterFactory::mountServicesFromExtensions)
        .map(OpenAPI3RouterFactory::getRouter)
        .doOnSuccess(this::addHystrixMetrics)
        .doOnSuccess(this::logRouterRoutes)
        .doOnSuccess(securityProvider::registerCustomRoutings)
        .flatMap(httpServerProvider::configureHttpServer)
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

  private void addHystrixMetrics(Router router) {
    HystrixMetricsOptions hystrixMetricsOptions = options.getHystrixMetricsOptions();
    if (hystrixMetricsOptions.isEnabled()) {
      router.get(hystrixMetricsOptions.getEndpoint()).handler(HystrixMetricHandler.create(vertx));
    }
  }

  private void logRouterRoutes(Router router) {
    LOGGER.info("Routes [{}]", router.getRoutes());
    printRoutes(router);
  }

  private void printRoutes(Router router) {
    // @formatter:off
    System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    System.out.println("@@                              ROUTER CONFIG                                 @@");
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    router.getRoutes().forEach(route -> System.out.println("@@     " + route.getDelegate()));
    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
    // @formatter:on
  }

}
