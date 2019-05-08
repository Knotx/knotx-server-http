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
package io.knotx.server.api.context;

import com.google.common.base.Objects;
import io.knotx.commons.http.request.DataObjectsUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.http.HttpServerRequest;
import java.util.List;
import java.util.Map;

/**
 * Contains details of currently processed Client Request
 */
@DataObject(generateConverter = true)
public class ClientRequest {

  private String path;

  private HttpMethod method;

  private MultiMap headers = MultiMap.caseInsensitiveMultiMap();

  private MultiMap params = MultiMap.caseInsensitiveMultiMap();

  private MultiMap formAttributes = MultiMap.caseInsensitiveMultiMap();

  public ClientRequest() {
    //Nothing to set by default
  }

  public ClientRequest(JsonObject json) {
    ClientRequestConverter.fromJson(json, this);
  }

  public ClientRequest(ClientRequest request) {
    this.path = request.path;
    this.method = request.method;
    this.headers = MultiMap.caseInsensitiveMultiMap().setAll(request.headers);
    this.params = MultiMap.caseInsensitiveMultiMap().setAll(request.params);
    this.formAttributes = MultiMap.caseInsensitiveMultiMap().setAll(request.formAttributes);
  }

  public ClientRequest(HttpServerRequest serverRequest) {
    this.path = serverRequest.path();
    this.method = serverRequest.method();
    this.headers = MultiMap.caseInsensitiveMultiMap().setAll(serverRequest.headers());
    this.params = paramsFromUri(serverRequest.uri());
    this.formAttributes = MultiMap.caseInsensitiveMultiMap().setAll(serverRequest.formAttributes());
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ClientRequestConverter.toJson(this, json);
    return json;
  }

  public String getPath() {
    return path;
  }

  /**
   * Path of the HTTP request.
   * @param path of the request
   * @return a reference to this, so the API can be used fluently
   */
  public ClientRequest setPath(String path) {
    this.path = path;
    return this;
  }

  public HttpMethod getMethod() {
    return method;
  }

  /**
   * Method of the HTTP request.
   * @param method of the request
   * @return a reference to this, so the API can be used fluently
   */
  public ClientRequest setMethod(HttpMethod method) {
    this.method = method;
    return this;
  }

  @GenIgnore
  public MultiMap getHeaders() {
    return MultiMap.caseInsensitiveMultiMap().addAll(headers);
  }

  @GenIgnore
  public ClientRequest setHeaders(MultiMap headers) {
    this.headers = MultiMap.caseInsensitiveMultiMap().addAll(headers);
    return this;
  }

  @GenIgnore
  public MultiMap getParams() {
    return MultiMap.caseInsensitiveMultiMap().addAll(params);
  }

  @GenIgnore
  public ClientRequest setParams(MultiMap params) {
    this.params = MultiMap.caseInsensitiveMultiMap().addAll(params);
    return this;
  }

  @GenIgnore
  public MultiMap getFormAttributes() {
    return MultiMap.caseInsensitiveMultiMap().addAll(formAttributes);
  }

  @GenIgnore
  public ClientRequest setFormAttributes(MultiMap formAttributes) {
    this.formAttributes = MultiMap.caseInsensitiveMultiMap().addAll(formAttributes);
    return this;
  }

  public JsonObject getJsonHeaders() {
    return MultiMapConverter.toJsonObject(headers);
  }

  /**
   * Headers of the HTTP request.
   * @param headers in form of {@link JsonObject}
   */
  public void setJsonHeaders(JsonObject headers) {
    this.headers = MultiMapConverter.fromJsonObject(headers);
  }

  public JsonObject getJsonParams() {
    return MultiMapConverter.toJsonObject(params);
  }

  /**
   * Params of the HTTP request.
   * @param params in form of {@link JsonObject}
   */
  public void setJsonParams(JsonObject params) {
    this.params = MultiMapConverter.fromJsonObject(params);
  }

  public JsonObject getJsonFormAttributes() {
    return MultiMapConverter.toJsonObject(formAttributes);
  }

  /**
   * Form Attributes of the HTTP request.
   * @param formAttributes in form of {@link JsonObject}
   */
  public void setJsonFormAttributes(JsonObject formAttributes) {
    this.formAttributes = MultiMapConverter.fromJsonObject(formAttributes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientRequest that = (ClientRequest) o;
    return Objects.equal(path, that.path) &&
        method == that.method &&
        DataObjectsUtil.equalsMultiMap(headers, that.headers) &&
        DataObjectsUtil.equalsMultiMap(params, that.params) &&
        DataObjectsUtil.equalsMultiMap(formAttributes, that.formAttributes);
  }

  @Override
  public int hashCode() {
    return 41 * Objects.hashCode(path, method) + 37 * DataObjectsUtil.multiMapHash(headers)
        + 31 * DataObjectsUtil.multiMapHash(params)
        + DataObjectsUtil.multiMapHash(formAttributes);
  }

  @Override
  public String toString() {
    return "ClientRequest{" +
        "path='" + path + '\'' +
        ", method=" + method +
        ", headers=" + DataObjectsUtil.toString(headers) +
        ", params=" + DataObjectsUtil.toString(params) +
        ", formAttributes=" + DataObjectsUtil.toString(formAttributes) +
        '}';
  }

  private MultiMap paramsFromUri(String uri) {
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
    Map<String, List<String>> queryParams = queryStringDecoder.parameters();

    io.vertx.core.MultiMap params = io.vertx.core.MultiMap.caseInsensitiveMultiMap();
    if (!queryParams.isEmpty()) {
      for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
        params.add(entry.getKey(), entry.getValue());
      }

    }
    return MultiMap.newInstance(params);
  }
}
