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

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

class SourceDefinition<T> {

  private final T source;
  private final Set<String> prefixes;
  private final Set<PlaceholderSubstitutor<T>> substitutors;

  SourceDefinition(T source, Set<String> prefixes,
      Set<PlaceholderSubstitutor<T>> substitutors) {
    this.source = source;
    this.prefixes = prefixes;
    this.substitutors = substitutors;
  }

  T getSource() {
    return source;
  }

  Set<PlaceholderSubstitutor<T>> getSubstitutors() {
    return substitutors;
  }

  Set<String> getPlaceholdersForSource(Set<String> allPlaceholders) {
    return allPlaceholders.stream()
        .filter(this::startsWith)
        .collect(Collectors.toSet());
  }

  private boolean startsWith(String placeholder) {
    return prefixes.contains(StringUtils.substringBefore(placeholder, "."));
  }
}
