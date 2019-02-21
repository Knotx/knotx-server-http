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

import io.knotx.fragment.Fragment;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestEvent {

  private static final String CLIENT_REQUEST_KEY = "clientRequest";
  private static final String FRAGMENTS_KEY = "fragments";
  private static final String PAYLOAD_KEY = "payload";
  
  private final ClientRequest clientRequest;
  private final List<Fragment> fragments;
  private final JsonObject payload;

  public RequestEvent(ClientRequest clientRequest, List<Fragment> fragments, JsonObject payload) {
    this.clientRequest = clientRequest;
    this.fragments = fragments;
    this.payload = payload;
  }

  public RequestEvent(ClientRequest clientRequest) {
    this.clientRequest = clientRequest;
    this.fragments = new ArrayList<>();
    this.payload = new JsonObject();
  }

  public RequestEvent(JsonObject json) {
    this.clientRequest = new ClientRequest(json.getJsonObject(CLIENT_REQUEST_KEY));
    this.fragments = json.getJsonArray(FRAGMENTS_KEY).stream()
        .map(JsonObject.class::cast)
        .map(Fragment::new)
        .collect(Collectors.toList());
    this.payload = json.getJsonObject(PAYLOAD_KEY);
  }

  public ClientRequest getClientRequest() {
    return clientRequest;
  }

  public List<Fragment> getFragments() {
    return fragments;
  }

  public JsonObject getPayload() {
    return payload.copy();
  }

  public JsonObject appendPayload(String key, Object value) {
    this.payload.put(key, value);
    return payload;
  }

  public JsonObject toJson() {
    final JsonArray fragmentsArray = new JsonArray();
    fragments.forEach(entry -> fragmentsArray.add(entry.toJson()));
    return new JsonObject()
        .put(CLIENT_REQUEST_KEY, clientRequest.toJson())
        .put(FRAGMENTS_KEY, fragmentsArray)
        .put(PAYLOAD_KEY, payload);
  }

  @Override
  public String toString() {
    return "RequestEvent{" +
        "clientRequest=" + clientRequest +
        ", fragments=" + fragments +
        ", payload=" + payload +
        '}';
  }
}
