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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class PlaceholdersResolver {

  private final SourceDefinitions sources;
  private final UnaryOperator<String> valueEncoding;
  private final boolean clearUnmatched;

  public PlaceholdersResolver(SourceDefinitions sources,
      UnaryOperator<String> valueEncoding, boolean clearUnmatched) {
    this.sources = sources;
    this.valueEncoding = valueEncoding;
    this.clearUnmatched = clearUnmatched;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static PlaceholdersResolver create(SourceDefinitions sources) {
    return builder()
        .withSources(sources)
        .build();
  }

  public static PlaceholdersResolver createEncoding(SourceDefinitions sources) {
    return builder()
        .withSources(sources)
        .encodeValues()
        .build();
  }

  public String resolve(String stringWithPlaceholders) {
    Set<String> currentPlaceholders = getPlaceholders(stringWithPlaceholders);

    List<String> searchList = new ArrayList<>();
    List<String> replaceList = new ArrayList<>();

    for (SourceDefinition sourceDefinition : sources.getSourceDefinitions()) {
      Set<String> placeholders = sourceDefinition.getPlaceholdersForSource(currentPlaceholders);
      for (String placeholder : placeholders) {
        searchList.add(placeholder);
        replaceList.add(getPlaceholderValue(sourceDefinition, placeholder));
      }
      currentPlaceholders.removeAll(placeholders);
    }

    if (clearUnmatched) {
      for (String placeholderLeft : currentPlaceholders) {
        searchList.add(placeholderLeft);
        replaceList.add("");
      }
    }

    String[] searchArray = searchList.stream().map(x -> "{" + x + "}").toArray(String[]::new);
    String[] replaceArray = replaceList.stream().map(valueEncoding).toArray(String[]::new);

    return StringUtils.replaceEach(stringWithPlaceholders, searchArray, replaceArray);
  }

  protected static Set<String> getPlaceholders(String serviceUri) {
    return Arrays.stream(serviceUri.split("\\{"))
        .filter(str -> str.contains("}"))
        .map(str -> StringUtils.substringBefore(str, "}"))
        .collect(Collectors.toSet());
  }

  private static <T> String getPlaceholderValue(SourceDefinition<T> sourceDefinition,
      String placeholder) {
    return sourceDefinition.getSubstitutors()
        .stream()
        .map(substitutor -> substitutor.getValue(sourceDefinition.getSource(), placeholder))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse("");
  }

  public static final class Builder {

    private SourceDefinitions sources;
    private UnaryOperator<String> valueEncoding = UnaryOperator.identity();
    private boolean clearUnmatched = true;

    public Builder withSources(SourceDefinitions sources) {
      this.sources = sources;
      return this;
    }

    public Builder encodeValues() {
      valueEncoding = Encoder::encode;
      return this;
    }

    public Builder leaveUnmatched() {
      clearUnmatched = false;
      return this;
    }

    public PlaceholdersResolver build() {
      if (sources == null) {
        throw new IllegalStateException(
            "Attempted to build PlaceholderResolver without setting sources");
      }
      return new PlaceholdersResolver(sources, valueEncoding, clearUnmatched);
    }

  }

}
