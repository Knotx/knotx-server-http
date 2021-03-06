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
package io.knotx.server.configuration;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Routing operation settings that define handlers / error handlers taking part in HTTP request
 * processing. {@link io.knotx.server.KnotxServerVerticle} loads {@link
 * KnotxServerOptions#getRoutingSpecificationLocation()} containing Open API specification which
 * describes all endpoints with request / response schemas. Each endpoint defines operationId used
 * in {@link RoutingOperationOptions#getOperationId()}.
 */
@DataObject(generateConverter = true, publicConverter = false)
public class RoutingOperationOptions {

  private String operationId;
  private List<RoutingHandlerOptions> handlers;
  private List<RoutingHandlerOptions> failureHandlers;

  /**
   * Create settings from JSON
   *
   * @param json the JSON
   */
  public RoutingOperationOptions(JsonObject json) {
    init();
    RoutingOperationOptionsConverter.fromJson(json, this);
    if (StringUtils.isBlank(operationId)) {
      throw new IllegalStateException(
          "Operation ID in routing configuration can not be null [" + json + "]");
    }
  }

  /**
   * Copy constructor
   *
   * @param other the instance to copy
   */
  public RoutingOperationOptions(RoutingOperationOptions other) {
    this.operationId = other.getOperationId();
    this.handlers = new ArrayList<>(other.getHandlers());
    this.failureHandlers = new ArrayList<>(other.getFailureHandlers());
  }

  private void init() {
    this.handlers = Collections.emptyList();
    this.failureHandlers = Collections.emptyList();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RoutingOperationOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * @return operationId name
   */
  public String getOperationId() {
    return operationId;
  }

  /**
   * Sets operationId name.
   *
   * @param operationId operation identifier
   * @return reference to this, so the API can be used fluently
   */
  public RoutingOperationOptions setOperationId(String operationId) {
    this.operationId = operationId;
    return this;
  }

  /**
   * @return list of handlers definitions used during HTTP request processing
   * @see io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addHandlerByOperationId(String,
   * Handler)
   */
  public List<RoutingHandlerOptions> getHandlers() {
    return handlers;
  }

  /**
   * Sets list of handlers definitions for particular operationId.
   *
   * @param handlers request handlers
   * @return reference to this, so the API can be used fluently
   * @see io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addHandlerByOperationId(String,
   * Handler)
   */
  public RoutingOperationOptions setHandlers(
      List<RoutingHandlerOptions> handlers) {
    this.handlers = handlers;
    return this;
  }

  /**
   * @return list of failure handlers definitions used during HTTP request processing
   * @see io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addFailureHandlerByOperationId(String,
   * Handler)
   */
  public List<RoutingHandlerOptions> getFailureHandlers() {
    return failureHandlers;
  }

  /**
   * Sets list of error handlers definitions for particular operationId.
   *
   * @param failureHandlers request failure handlers
   * @return reference to this, so the API can be used fluently
   * @see io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory#addFailureHandlerByOperationId(String,
   * Handler)
   */
  public RoutingOperationOptions setFailureHandlers(
      List<RoutingHandlerOptions> failureHandlers) {
    this.failureHandlers = failureHandlers;
    return this;
  }

  @Override
  public String toString() {
    return "RoutingOperationOptions{" +
        "operationId='" + operationId + '\'' +
        ", handlers=" + handlers +
        ", failureHandlers=" + failureHandlers +
        '}';
  }
}
