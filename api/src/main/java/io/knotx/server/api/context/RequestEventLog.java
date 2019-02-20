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
package io.knotx.server.api.context;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestEventLog {

  private final List<Entry> operations;

  RequestEventLog() {
    operations = new ArrayList<>();
  }

  RequestEventLog(JsonObject json) {
    operations = json.getJsonArray("operations").stream()
        .map(JsonObject.class::cast)
        .map(Entry::new)
        .collect(Collectors.toList());
  }

  public JsonObject toJson() {
    final JsonArray jsonArray = new JsonArray();
    operations.forEach(entry -> jsonArray.add(entry.toJson()));
    return new JsonObject()
        .put("operations", jsonArray);
  }

  List<Entry> getOperations() {
    return operations;
  }

  void append(String handlerId, Status status, String errorMessage) {
    operations.add(new Entry(handlerId, status, errorMessage));
  }

  void append(String handlerId, Status status) {
    operations.add(new Entry(handlerId, status, null));
  }

  @Override
  public String toString() {
    return "RequestEventLog{" +
        "operations=" + operations +
        '}';
  }

  static class Entry {

    private String handlerId;
    private Status status;
    private String errorMessage;
    private long timestamp;

    private Entry(String handlerId, Status status, String errorMessage) {
      this.handlerId = handlerId;
      this.status = status;
      this.errorMessage = errorMessage;
      this.timestamp = System.currentTimeMillis();
    }

    Entry(JsonObject json) {
      this.handlerId = json.getString("handlerId");
      this.status = Status.valueOf(json.getString("status"));
      this.errorMessage = json.getString("errorMessage");
      this.timestamp = json.getLong("timestamp");
    }

    JsonObject toJson() {
      return new JsonObject()
          .put("handlerId", handlerId)
          .put("status", status)
          .put("errorMessage", errorMessage)
          .put("timestamp", timestamp);
    }

    public String getHandlerId() {
      return handlerId;
    }

    public Status getStatus() {
      return status;
    }

    public Optional<String> getErrorMessage() {
      return Optional.ofNullable(errorMessage);
    }

    public long getTimestamp() {
      return timestamp;
    }

    @Override
    public String toString() {
      return "Entry{" +
          "handlerId='" + handlerId + '\'' +
          ", status=" + status +
          ", errorMessage='" + errorMessage + '\'' +
          ", timestamp=" + timestamp +
          '}';
    }
  }

  enum Status {
    SUCCESS, FAILURE, FATAL
  }
}
