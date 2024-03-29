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
package io.knotx.server.security;

import io.knotx.server.api.security.AuthHandlerFactory;
import io.vertx.core.json.JsonObject;
//import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.reactivex.core.Vertx;
//import io.vertx.reactivex.ext.auth.shiro.ShiroAuth;
import io.vertx.reactivex.ext.web.handler.AuthHandler;
import io.vertx.reactivex.ext.web.handler.BasicAuthHandler;

public class BasicAuthHandlerFactory implements AuthHandlerFactory {

  @Override
  public String getName() {
    return "testBasicAuth";
  }

  @Override
  public AuthHandler create(Vertx vertx, JsonObject config) {
    // FIXME https://github.com/Knotx/knotx-server-http/issues/76
//    final ShiroAuth shiroAuth = ShiroAuth.create(vertx, new ShiroAuthOptions().setConfig(config));
//    return BasicAuthHandler.create(shiroAuth);
    return null;
  }
}
