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
package io.knotx.splitter.html;

import io.knotx.fragment.Fragment;
import io.knotx.server.api.context.ClientResponse;
import io.knotx.server.api.context.RequestEvent;
import io.knotx.server.api.handler.RequestEventHandlerResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

class ClientResponseBodyExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientResponseBodyExtractor.class);
  private static final String MISSING_REPOSITORY_PAYLOAD = "Template body is missing!";

  private final HtmlFragmentSplitter splitter;

  ClientResponseBodyExtractor() {
    splitter = new HtmlFragmentSplitter();
  }

  //for unit tests
  ClientResponseBodyExtractor(HtmlFragmentSplitter splitter) {
    this.splitter = splitter;
  }

  RequestEventHandlerResult splitAndClearBody(RequestEvent requestEvent, ClientResponse clientResponse) {
    final RequestEventHandlerResult result;
    final String template = getTemplate(clientResponse);
    if (StringUtils.isNotBlank(template)) {
      List<Fragment> fragments = splitter.split(template);
      RequestEvent requestEventWithFragments = new RequestEvent(requestEvent.getClientRequest(),
          fragments, requestEvent.getPayload());
      result = RequestEventHandlerResult.success(requestEventWithFragments);
      clientResponse.setBody(null);
    } else {
      LOGGER.error(MISSING_REPOSITORY_PAYLOAD);
      result = RequestEventHandlerResult.fail(MISSING_REPOSITORY_PAYLOAD);
    }
    return result;
  }

  private String getTemplate(ClientResponse clientResponse) {
    return Optional.ofNullable(clientResponse.getBody()).map(Buffer::toString)
        .orElse(null);
  }

}
