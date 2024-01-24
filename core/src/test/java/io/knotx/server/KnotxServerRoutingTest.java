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

import static io.restassured.RestAssured.given;

import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests are created according to Open API 3.0 Spec: <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md">...</a>
 */
@ExtendWith(KnotxExtension.class)
@KnotxApplyConfiguration({"server.conf", "server-random-port.conf", "routing/routing.conf"})
class KnotxServerRoutingTest {

  @Test
  @DisplayName("Expect Ok when requesting defined and configured URL")
  void callServer(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
        get("/test/any").
    then().assertThat().
        statusCode(HttpResponseStatus.OK.code());
    // @formatter:on
  }

  @Test
  @DisplayName("Expect NOT FOUND when requested URL is not defined in Open API spec.")
  void notConfiguredURLNotFound(Vertx vertx, @RandomPort Integer globalServerPort) {
    given().
        port(globalServerPort).
    when().
        get("/not/defined").
    then().assertThat().
        statusCode(HttpResponseStatus.NOT_FOUND.code());
  }

  @Test
  @DisplayName("Expect NOT IMPLEMENTED when defined in Open API spec but no routing handlers defined.")
  void notImplementedRoutingLogic(Vertx vertx, @RandomPort Integer globalServerPort) {
    given().
        port(globalServerPort).
    when().
        get("/not/ready/endpoint").
    then().assertThat().
        statusCode(HttpResponseStatus.NOT_IMPLEMENTED.code());
  }

  @Test
  @Disabled("ToDo - should be implemented")
  @DisplayName("Expect INTERNAL SERVER ERROR no handler on the route ends the response.")
  void noHandlerEndsResponse(Vertx vertx, @RandomPort Integer globalServerPort) {
    given().
        port(globalServerPort).
    when().
        get("/misconfigured/endpoint").
    then().assertThat().
        statusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }


}
