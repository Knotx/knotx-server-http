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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests are created according to Open API 3.0 Spec: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md
 */
@ExtendWith(KnotxExtension.class)
@KnotxApplyConfiguration({"server.conf", "server-random-port.conf", "security/security.conf"})
class KnotxServerSecurityTest {

  private static final String BASIC_AUTH_ENDPOINT_URL = "/protected/basic";
  private static final String API_KEY_ENDPOINT_URL = "/protected/apiKey";
  private static final String JWT_ENDPOINT_URL = "/protected/jwt";
  private static final String OAUTH_ENDPOINT_URL = "/protected/oAuth";

  @Test
  @DisplayName("Expect Ok - call public endpoint")
  void callPublicEndpoint(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
        get("/public").
    then().assertThat().
        statusCode(200);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Ok when basic authentication passes.")
  @Disabled
  // FIXME https://github.com/Knotx/knotx-server-http/issues/76
  void basicAuthenticationSuccess(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
        header("Authorization", "Basic am9objpzM2NyM3Q=").
    when().
      get(BASIC_AUTH_ENDPOINT_URL).
    then().assertThat().
        statusCode(200);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Unauthorized when basic authentication fails.")
  @Disabled
  // FIXME https://github.com/Knotx/knotx-server-http/issues/76
  void basicAuthenticationFailure(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
      get(BASIC_AUTH_ENDPOINT_URL).
    then().assertThat().
        statusCode(401);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Ok when API key authentication passes.")
  @Disabled("To Do")
  void apiKeyAuthenticationSuccess(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    //  header(API_KEY)
    when().
      get(API_KEY_ENDPOINT_URL).
    then().assertThat().
        statusCode(200);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Unauthorized when API key authentication fails.")
  @Disabled("To Do")
  void apiKeyAuthenticationFailure(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
      get(API_KEY_ENDPOINT_URL).
    then().assertThat().
        statusCode(401);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Ok when JWT authentication passes.")
  void jwtAuthenticationSuccess(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
        header("Authorization", generateJWTHeader(vertx)).
    when().
      get(JWT_ENDPOINT_URL).
    then().assertThat().
        statusCode(200);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Unauthorized when JWT authentication fails.")
  void jwtAuthenticationFailure(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
      get(JWT_ENDPOINT_URL).
    then().assertThat().
        statusCode(401);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Ok when OAuth 2.0 authentication passes.")
  @Disabled("To Do")
  void oAuthAuthenticationSuccess(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    //  header(OAuth)
    when().
      get(OAUTH_ENDPOINT_URL).
    then().assertThat().
        statusCode(200);
    // @formatter:on
  }

  @Test
  @DisplayName("Expect Unauthorized when OAuth 2.0 authentication fails.")
  @Disabled("To Do")
  void oAuthAuthenticationFailure(Vertx vertx, @RandomPort Integer globalServerPort) {
    // @formatter:off
    given().
        port(globalServerPort).
    when().
      get(OAUTH_ENDPOINT_URL).
    then().assertThat().
        statusCode(401);
    // @formatter:on
  }

  private String generateJWTHeader(Vertx vertx) {
    final KeyStoreOptions keyStore = new KeyStoreOptions()
        .setType("jceks")
        .setPath("security/keystore.jceks")
        .setPassword("secret");
    final JWTAuthOptions config = new JWTAuthOptions().setKeyStore(keyStore);

    final JWTAuth jwtAuth = JWTAuth.create(vertx, config);
    final String token = jwtAuth
        .generateToken(new JsonObject().put("name", "john"), new JWTOptions().setExpiresInSeconds(60));
    return "Bearer " + token;
  }
}
