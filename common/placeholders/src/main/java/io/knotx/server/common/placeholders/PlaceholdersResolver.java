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

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class PlaceholdersResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholdersResolver.class);

  private final SourceDefinitions sources;
  private final UnaryOperator<String> valueEncoding;
  private final boolean clearUnmatched;

  public PlaceholdersResolver(SourceDefinitions sources,
      UnaryOperator<String> valueEncoding, boolean clearUnmatched) {
    this.sources = sources;
    this.valueEncoding = valueEncoding;
    this.clearUnmatched = clearUnmatched;
  }

  private static Builder builder() {
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

  public static PlaceholdersResolver createEncodingAndSkippingUnmatched(SourceDefinitions sources) {
    return builder()
        .withSources(sources)
        .encodeValues()
        .leaveUnmatched()
        .build();
  }

  public static String resolveSkipUnmatched(String stringWithPlaceholders, SourceDefinitions sources) {
    return createEncodingAndSkippingUnmatched(sources).resolveAndEncodeInternal(stringWithPlaceholders);
  }

  public static String resolve(String stringWithPlaceholders, SourceDefinitions sources) {
    return create(sources).resolveAndEncodeInternal(stringWithPlaceholders);
  }

  public static String resolveAndEncode(String stringWithPlaceholders, SourceDefinitions sources) {
    return createEncoding(sources).resolveAndEncodeInternal(stringWithPlaceholders);
  }

  private String resolveAndEncodeInternal(String stringWithPlaceholders) {
    String resolved = stringWithPlaceholders;
    List<String> allPlaceholders = getPlaceholders(stringWithPlaceholders);

    for (SourceDefinition sourceDefinition : sources.getSourceDefinitions()) {
      resolved = resolveAndEncode(resolved, allPlaceholders, sourceDefinition);
    }

    if (clearUnmatched) {
      resolved = clearUnmatched(resolved);
    }

    return resolved;
  }

  private <T> String resolveAndEncode(String resolved, List<String> allPlaceholders,
      SourceDefinition<T> sourceDefinition) {
    List<String> placeholders = sourceDefinition.getPlaceholdersForSource(allPlaceholders);
    for (String placeholder : placeholders) {
      resolved = replaceAndEncode(resolved, placeholder,
          getPlaceholderValue(sourceDefinition, placeholder));
    }
    return resolved;
  }

  private static String clearUnmatched(String resolved) {
    List<String> unresolved = getPlaceholders(resolved);

    for (String unresolvedPlaceholder : unresolved) {
      resolved = replace(resolved, unresolvedPlaceholder, "");
    }
    return resolved;
  }

  private String replaceAndEncode(String resolved, String placeholder, String value) {
    return replace(resolved, placeholder, valueEncoding.apply(value));
  }

  private static String replace(String resolved, String placeholder, String value) {
    return resolved.replace("{" + placeholder + "}", value);
  }

  protected static List<String> getPlaceholders(String serviceUri) {
    return Arrays.stream(serviceUri.split("\\{"))
        .filter(str -> str.contains("}"))
        .map(str -> StringUtils.substringBefore(str, "}"))
        .collect(Collectors.toList());
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

  static final class Builder {
    private SourceDefinitions sources;
    private UnaryOperator<String> valueEncoding = UnaryOperator.identity();
    private boolean clearUnmatched = true;

    Builder withSources(SourceDefinitions sources) {
      this.sources = sources;
      return this;
    }

    Builder encodeValues() {
      valueEncoding = Encoder::encode;
      return this;
    }

    Builder leaveUnmatched() {
      clearUnmatched = false;
      return this;
    }

    PlaceholdersResolver build() {
      if (sources == null) {
        throw new IllegalStateException("Attempted to build PlaceholderResolver without setting sources");
      }
      return new PlaceholdersResolver(sources, valueEncoding, clearUnmatched);
    }

  }

}
