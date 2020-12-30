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

  public PlaceholdersResolver(SourceDefinitions sources, UnaryOperator<String> valueEncoding) {
    this(sources, valueEncoding, true);
  }

  public PlaceholdersResolver(SourceDefinitions sources) {
    this(sources, UnaryOperator.identity(), true);
  }

  public static String resolveSkipUnmatched(String stringWithPlaceholders, SourceDefinitions sources) {
    return new PlaceholdersResolver(sources, UnaryOperator.identity(), false).resolveAndEncodeInternal(stringWithPlaceholders);
  }

  public static String resolve(String stringWithPlaceholders, SourceDefinitions sources) {
    return new PlaceholdersResolver(sources).resolveAndEncodeInternal(stringWithPlaceholders);
  }

  public static String resolveAndEncode(String stringWithPlaceholders, SourceDefinitions sources) {
    return new PlaceholdersResolver(sources, PlaceholdersResolver::encodeValue)
        .resolveAndEncodeInternal(stringWithPlaceholders);
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

  private static String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8")
          .replace("+", "%20")
          .replace("%2F", "/");
    } catch (UnsupportedEncodingException ex) {
      LOGGER.fatal("Unexpected Exception - Unsupported encoding UTF-8", ex);
      throw new UnsupportedCharsetException("UTF-8");
    }
  }
}
