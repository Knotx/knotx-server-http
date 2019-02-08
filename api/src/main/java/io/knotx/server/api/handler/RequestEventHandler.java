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
package io.knotx.server.api.handler;

import io.knotx.server.api.context.RequestContext;
import io.knotx.server.api.context.RequestEvent;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * A generic Fragment processing handler.
 */
public abstract class RequestEventHandler implements Handler<RoutingContext> {

  private final RequestContextEngine engine;

  public RequestEventHandler() {
    this.engine = new DefaultRequestContextEngine(getClass().getSimpleName());
  }

  public RequestEventHandler (RequestContextEngine engine) {
    this.engine = engine;
  }

  @Override
  public void handle(RoutingContext context) {
    RequestContext requestContext = context.get(RequestContext.KEY);
    try {
      RequestEventHandlerResult result = handle(requestContext.getRequestEvent());
      engine.processAndSaveResult(result, context, requestContext);
    } catch (Exception e) {
      engine.handleFatal(context, requestContext, e);
    }
  }

  /**
   * Handle RequestEvent processing
   *
   * @param requestEvent - knot.x fragment context from the previous handler
   * @return RequestEventHandlerResult that contains handling result
   */
  protected abstract RequestEventHandlerResult handle(RequestEvent requestEvent);



}
