/*
 * Copyright (C) 2018 Knot.x Project
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
package io.knotx.server.common.placeholders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonResolverTest {

  private static final JsonObject PLAIN_JSON = new JsonObject()
      .put("key1", "{payload.value1}")
      .put("key2", "{payload.not.existent}")
      .put("key3", "{non.existent.value2}")
      .put("{payload.key1}", "plainvalue")
      .put("{payload.not.existent.key}", "plainvalue")
      .put("{not.existent.key}", "plainvalue}");

  private static final JsonObject NESTED_JSON = new JsonObject()
      .put("key1", "{payload.value1}")
      .put("key2", new JsonArray()
          .add(new JsonObject().put("key3", true))
          .add(15)
          .add("{placeholder}")
      );

  @Mock
  private PlaceholdersResolver resolver;

  private JsonResolver tested;

  @BeforeEach
  void setUp() {
    when(resolver.resolve(anyString())).thenReturn("somevalue");
    tested = new JsonResolver(resolver);
  }

  @Test
  @DisplayName("Expect 12 calls to resolver for 12 keys and values")
  void shouldCallResolverForEachPlaceholder() {
    tested.resolveJson(PLAIN_JSON);

    verify(resolver, times(12)).resolve(anyString());
  }

  @Test
  @DisplayName("Expect 5 calls to resolver for 5 string values")
  void shouldCallResolverForEachPlaceholderForNested() {
    tested.resolveJson(NESTED_JSON);

    verify(resolver, times(5)).resolve(anyString());
  }

}
