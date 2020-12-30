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

final class Encoder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Encoder.class);

  private Encoder() {
    // utility class
  }

  static String encode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8")
          .replace("+", "%20")
          .replace("%2F", "/");
    } catch (UnsupportedEncodingException ex) {
      LOGGER.fatal("Unexpected Exception - Unsupported encoding UTF-8", ex);
      throw new UnsupportedCharsetException("UTF-8");
    }
  }

}
