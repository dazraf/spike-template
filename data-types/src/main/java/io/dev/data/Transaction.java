package io.dev.data;

import io.vertx.core.json.JsonObject;

public class Transaction {
  private final JsonObject obj;

  public Transaction(JsonObject jsonObject) {
    obj = jsonObject;
  }
}
