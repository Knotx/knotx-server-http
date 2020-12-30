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

import io.knotx.server.api.context.ClientRequest;
import io.vertx.reactivex.core.MultiMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PlaceholdersResolverReplaceTest {

  private static final String STRING_WITH_SPECIAL_CHARS = "Q@T(#&SGESOGJW$*T#)WIGoiw4yh902j";
  private static final String ESCAPED_STRING_WITH_SPECIAL_CHARS = "Q%40T%28%23%26SGESOGJW%24*T%23%29WIGoiw4yh902j";

  /**
   * Data source for following test
   */
  static Stream<Arguments> data() {
    return Stream.of(
        // SLING URI DECOMPOSITION
        // path
        Arguments.of("/path.html?path={slingUri.path}", "/a/b/c/d.s1.s2.html/c/d.s.txt#f",
            "/path.html?path=/a/b/c/d"),
        Arguments.of("/path.html?path={slingUri.path}", "/a/b/c/d.s1.s2.html/c/d.s.txt#f",
            "/path.html?path=/a/b/c/d"),
        // pathparts
        Arguments.of("/path/{slingUri.pathpart[2]}.html", "/a/b/c/d/e.s1.s2.html/c/d.s.txt#f",
            "/path/c.html"),
        Arguments.of("/path/{slingUri.pathpart[7]}.html", "/a/b/c/d/e.s1.s2.html/c/d.s.txt#f",
            "/path/.html"),
        // extension
        Arguments.of("/path/second.html/a.{slingUri.extension}", "/a/b/c/d/e.s1.s2.html/suffix.xml",
            "/path/second.html/a.html"),
        // selectors
        Arguments.of("/selectors.{slingUri.selectorstring}.html", "/a/b.s1.s2.html/c/d.s.txt#f",
            "/selectors.s1.s2.html"),
        Arguments.of("/selectors.{slingUri.selector[0]}.html", "/a/b.s1.s2.html/c/d.s.txt#f",
            "/selectors.s1.html"),
        Arguments.of("/selectors.{slingUri.selector[2]}.html", "/a/b.s1.s2.html/c/d.s.txt#f",
            "/selectors..html"),
        // suffix
        Arguments.of("/suffix.html{slingUri.suffix}", "/a/b/dsds.dd.html/my/nice/suffix.html",
            "/suffix.html/my/nice/suffix.html"),
        // REGULAR URI DECOMPOSITION
        // path
        Arguments.of("/path.html?path={uri.path}", "/a/b/c/d.s1.s2.html/c/d.s.txt#f",
            "/path.html?path=/a/b/c/d.s1.s2.html/c/d.s.txt"),
        // pathpart
        Arguments.of("/path/{uri.pathpart[5]}.html", "/a/b/c/d/e.s1.s2.html/c/d.s.txt#f",
            "/path/e.s1.s2.html.html"),
        // pathpart
        Arguments.of("/path/{uri.pathpart[7]}.html", "/a/b/c/d/e.s1.s2.html/c/d.s.txt#f",
            "/path/d.s.txt.html"),
        // pathpart
        Arguments.of("/path/{uri.pathpart[8]}.html", "/a/b/c/d/e.s1.s2.html/c/d.s.txt#f",
            "/path/.html"),
        // extension
        Arguments.of("/path/second.html/a.{uri.extension}", "/a/b.s1.s2.html/c/d.xml",
            "/path/second.html/a.xml"),
        // extension
        Arguments.of("/path/second.html/a.{uri.extension}", "/a/b",
            "/path/second.html/a."),
        // param
        Arguments
            .of("/solr/search/{param.q}", "/c/d/s?q=my search is fetched from static getParams()",
                "/solr/search/core%20%26%20x"),
        // headers
        Arguments.of("/solr/{header.authorizationId}/", "/c/d/s?q=my action from headers",
            "/solr/486434684345/"),
        // invalid
        Arguments
            .of("/selectors.{invalid}.html", "/a/b.s1.s2.html/c/d.s.txt#f", "/selectors..html"));
  }

  private static MultiMap getHeadersMultiMap() {
    return MultiMap.caseInsensitiveMultiMap()
        .add("authorizationId", "486434684345");
  }

  private static MultiMap getParamsMultiMap() {
    return MultiMap.caseInsensitiveMultiMap()
        .add("q", "core & x")
        .add("action", "/some/action/path");
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("data")
  void getServiceUri_whenGivenUriWithPlaceholdersAndMockedRequest_expectPlaceholdersSubstitutedWithValues(
      String servicePath, String requestedUri, String expectedUri) {
    ClientRequest httpRequest = new ClientRequest().setHeaders(getHeadersMultiMap())
        .setParams(getParamsMultiMap())
        .setPath(requestedUri);

    SourceDefinitions sourceDefinitions = SourceDefinitions.builder()
        .addClientRequestSource(httpRequest)
        .build();
    String finalUri = PlaceholdersResolver.resolveAndEncode(servicePath, sourceDefinitions);

    Assertions.assertEquals(expectedUri, finalUri);
  }

  @Test
  @DisplayName("Expect escaped value to be populated for the placeholder")
  void resolveAndEncode() {
    String finalUri = PlaceholdersResolver
        .resolveAndEncode("{param.special}", sourceWithParam(STRING_WITH_SPECIAL_CHARS));

    Assertions.assertEquals(ESCAPED_STRING_WITH_SPECIAL_CHARS, finalUri);
  }

  @Test
  @DisplayName("Expect value to be populated for the placeholder but not escaped")
  void resolveAndDoNotEncode() {
    String finalUri = PlaceholdersResolver
        .resolve("{param.special}", sourceWithParam(STRING_WITH_SPECIAL_CHARS));

    Assertions.assertEquals(STRING_WITH_SPECIAL_CHARS, finalUri);
  }

  @Test
  @DisplayName("Expect placeholder enclosed with extra brackets to be interpolated")
  void extraBracketsNotReplaced() {
    String finalUri = PlaceholdersResolver
        .resolve("{\"json-key\": \"{param.special}\"}", sourceWithParam("test"));

    Assertions.assertEquals("{\"json-key\": \"test\"}", finalUri);
  }

  private SourceDefinitions sourceWithParam(String value) {
    ClientRequest httpRequest = new ClientRequest().setHeaders(getHeadersMultiMap())
        .setParams(getParamsMultiMap().add("special", value))
        .setPath("/test/");

    return SourceDefinitions.builder()
        .addClientRequestSource(httpRequest)
        .build();
  }
}
