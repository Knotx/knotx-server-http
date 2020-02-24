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
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@KnotxApplyConfiguration({"server.conf", "server-random-port.conf", "headers/headers.conf"})
class KnotxServerResponseHeadersTest {

  @Test
  @DisplayName("Should contain X-Server and content-length headers in server response")
  void shouldContainResponseHeaders(Vertx vertx, @RandomPort Integer globalServerPort) {
    given()
        .port(globalServerPort)
        .when()
        .get("/api/headers")
        .then()
        .assertThat()
        .header("X-Server", "Knot.x-Custom-Header")
        .header("content-length", "0");
  }
}
