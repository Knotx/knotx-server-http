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
package io.knotx.server.common.placeholders;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class JsonResolver {

  private final PlaceholdersResolver resolver;

  public JsonResolver(PlaceholdersResolver resolver) {
    this.resolver = resolver;
  }

  public JsonObject resolveJson(JsonObject input) {
    JsonObject output = new JsonObject();
    input.forEach(
        entry -> output.put(resolveNotEmpty(entry.getKey()), resolveInternal(entry.getValue())));
    return output;
  }

  private String resolveNotEmpty(String input) {
    return Optional.of(input)
        .map(resolver::resolve)
        .filter(StringUtils::isNotEmpty)
        .orElseThrow(() -> new IllegalStateException(
            String.format("Resolving [%s] resulted in a forbidden empty string", input)));
  }

  private Object resolveInternal(Object object) {
    if (object instanceof JsonObject) {
      return resolveJson((JsonObject) object);
    } else if (object instanceof JsonArray) {
      JsonArray array = (JsonArray) object;
      List<Object> list = array.stream().map(this::resolveInternal).collect(Collectors.toList());
      return new JsonArray(list);
    } else if (object instanceof String) {
      return resolver.resolve((String) object);
    } else {
      return object;
    }
  }

}
