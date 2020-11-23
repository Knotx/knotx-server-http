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

import io.knotx.server.api.security.AuthHandlerFactory;
import io.knotx.server.configuration.AuthHandlerOptions;
import io.knotx.server.exceptions.ConfigurationException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

class SecurityProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityProvider.class);
  private final Vertx vertx;
  private final List<AuthHandlerOptions> securityHandlers;

  private Map<String, AuthHandlerFactory> authHandlerFactoriesByName;

  SecurityProvider(Vertx vertx,
      List<AuthHandlerOptions> securityHandlers) {
    this.vertx = vertx;
    this.securityHandlers = securityHandlers;
    this.authHandlerFactoriesByName = loadAuthHandlerFactories();
  }

  void configureSecurity(OpenAPI3RouterFactory routerFactory) {
    securityHandlers.forEach(options -> {
      registerAuthHandler(routerFactory,
          authHandlerFactoriesByName.get(options.getFactory()), options);
      LOGGER.info("Security handler [{}] initialized", options.getSchema());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Auth handler initialization details [{}]", options.toJson().encodePrettily());
      }
    });
  }

  void registerCustomRoutings(Router router) {
    authHandlerFactoriesByName.values().forEach(authHandlerFactory -> authHandlerFactory.registerCustomRoute(router));
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

}
