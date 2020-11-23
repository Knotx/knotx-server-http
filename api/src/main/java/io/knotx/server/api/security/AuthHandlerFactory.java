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
package io.knotx.server.api.security;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.AuthHandler;

/**
 * Defines a factory object that provides authentication handler instance with the particular name.
 */
public interface AuthHandlerFactory {

  /**
   * The handler name that is used in server security handlers configuration.
   * @return the handler name
   */
  String getName();

  /**
   * Creates authentication handler instance.
   * @param vertx vertx instance
   * @param config handler configuration
   * @return handler instance
   */
  AuthHandler create(Vertx vertx, JsonObject config);

  /**
   * Register custom routing required by AuthHandler
   * @param router
   */
  default void registerCustomRoute(Router router) {
  }

}
