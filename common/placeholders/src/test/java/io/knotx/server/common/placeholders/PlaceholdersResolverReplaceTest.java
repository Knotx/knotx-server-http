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
import io.vertx.core.json.JsonObject;
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

    String finalUri = PlaceholdersResolver
        .createEncoding(sourceDefinitions)
        .resolve(servicePath);

    Assertions.assertEquals(expectedUri, finalUri);
  }

  @Test
  @DisplayName("Expect escaped value to be populated for the placeholder")
  void resolveAndEncode() {
    String finalUri = PlaceholdersResolver
        .createEncoding(sourceWithParam(STRING_WITH_SPECIAL_CHARS))
        .resolve("{param.special}");

    Assertions.assertEquals(ESCAPED_STRING_WITH_SPECIAL_CHARS, finalUri);
  }

  @Test
  @DisplayName("Expect value to be populated for the placeholder but not escaped")
  void resolveAndDoNotEncode() {
    String finalUri = PlaceholdersResolver
        .create(sourceWithParam(STRING_WITH_SPECIAL_CHARS))
        .resolve("{param.special}");

    Assertions.assertEquals(STRING_WITH_SPECIAL_CHARS, finalUri);
  }

  @Test
  @DisplayName("Expect placeholder enclosed with extra brackets to be interpolated")
  void extraBracketsNotReplaced() {
    String finalUri = resolverEncodingAndSkippingUnmatched(sourceWithParam("test"))
        .resolve("{\"json-key\": \"{param.special}\"}");

    Assertions.assertEquals("{\"json-key\": \"test\"}", finalUri);
  }

  @Test
  @DisplayName("Expect no substitution of an already substituted value")
  void noDoubleSubstitution() {
    String finalUri = PlaceholdersResolver
        .create(sourceWithParam("{this.was.substituted}"))
        .resolve("{\"json-key\": \"{param.special}\"}");

    Assertions.assertEquals("{\"json-key\": \"{this.was.substituted}\"}", finalUri);
  }

  @Test
  @DisplayName("Expect not populated placeholder (but matched to a source) to be removed")
  void removeNotPopulatedPlaceholder() {
    final String notMatchedPlaceholder = "{param.notPopulated}";
    String finalUri = PlaceholdersResolver.create(sourceWithParam("test"))
        .resolve(notMatchedPlaceholder);

    Assertions.assertEquals("", finalUri);
  }

  @Test
  @DisplayName("Expect unmatched placeholders to be removed by default")
  void removeUnmatchedPlaceholder() {
    final String notMatchedPlaceholder = "{notMatchedParam}";
    String finalUri = PlaceholdersResolver
        .create(sourceWithParam("test"))
        .resolve(notMatchedPlaceholder);

    Assertions.assertEquals("", finalUri);
  }

  @Test
  @DisplayName("Expect unmatched placeholders to be left as-is if option configured")
  void leaveUnresolvedPlaceholders() {
    final String notMatchedPlaceholder = "{notMatchedParam}";
    String finalUri = resolverEncodingAndSkippingUnmatched(sourceWithParam("test"))
        .resolve(notMatchedPlaceholder);

    Assertions.assertEquals(notMatchedPlaceholder, finalUri);
  }

  /*
    This test prevents the following chain of events:
    - Input string "{source1.key1}-{source2.key2}" is given
    - Source1 replaces "{source1.key1}" with "some-prefix-{source2.key2}-some-suffix"
    - Source2 replaces "some-prefix-{source2.key2}-some-suffix" with "some-prefix-UNEXPECTED-some-suffix"
    We want to eliminate this possibility, because the order in which the sources are evaluated is not deterministic.
    This behaviour is an additional complexity which is not intuitive and hard to document.
    Therefore we try to enforce a single layer of substitution by this test.
   */
  @Test
  @DisplayName("Expect simultaneous substitution from different sources")
  void simultanousSubstitution() {
    final String singlePlaceholder = "{source1.key1}-{source2.key2}";

    SourceDefinitions sources = SourceDefinitions.builder()
        .addJsonObjectSource(new JsonObject().put("key1", "prefix-{source2.key2}-suffix"),
            "source1")
        .addJsonObjectSource(new JsonObject().put("key2", "ordinary-value"), "source2")
        .build();

    String finalUri = PlaceholdersResolver.create(sources)
        .resolve(singlePlaceholder);

    Assertions.assertEquals("prefix-{source2.key2}-suffix-ordinary-value", finalUri);
  }

  private SourceDefinitions sourceWithParam(String value) {
    ClientRequest httpRequest = new ClientRequest().setHeaders(getHeadersMultiMap())
        .setParams(getParamsMultiMap().add("special", value))
        .setPath("/test/");

    return SourceDefinitions.builder()
        .addClientRequestSource(httpRequest)
        .build();
  }

  private PlaceholdersResolver resolverEncodingAndSkippingUnmatched(SourceDefinitions sources) {
    return PlaceholdersResolver.builder()
        .withSources(sources)
        .encodeValues()
        .leaveUnmatched()
        .build();
  }
}
