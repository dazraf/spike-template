package io.dev.someservice.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * a data object for our api
 * this could do with code generation
 */
@DataObject
public class Transaction {
  public static String FIELD_ID = "id";
  public static String FIELD_AMOUNT = "amount";
  public static String FIELD_FROM = "from";
  public static String FIELD_TO = "to";

  private String id;
  private String fromAccount = "";
  private String toAccount = "";
  private double amount;

  public Transaction() {

  }

  public Transaction(JsonObject jsonObject) {
    this.id = jsonObject.getString(FIELD_ID, null);
    this.amount = jsonObject.getDouble(FIELD_AMOUNT);
    this.fromAccount = jsonObject.getString(FIELD_FROM);
    this.toAccount = jsonObject.getString(FIELD_TO);
  }

  public Transaction(Transaction src) {
    this.id = src.id;
    this.amount = src.amount;
    this.fromAccount = src.fromAccount;
    this.toAccount = src.toAccount;
  }

  public Transaction(String fromAccount, String toAccount, double amount) {
    this(null, fromAccount, toAccount, amount);
  }

  public Transaction(String id, String fromAccount, String toAccount, double amount) {
    this.id = id;
    this.fromAccount = fromAccount;
    this.toAccount = toAccount;
    this.amount = amount;
  }

  public String getId() {
    return id;
  }

  public Transaction setId(String id) {
    this.id = id;
    return this;
  }

  public String getFromAccount() {
    return fromAccount;
  }

  public Transaction setFromAccount(String account) {
    this.fromAccount = account;
    return this;
  }

  public String getToAccount() {
    return toAccount;
  }

  public Transaction setToAccount(String account) {
    this.toAccount = account;
    return this;
  }

  public double getAmount() {
    return amount;
  }

  public Transaction setAmount(double amount) {
    this.amount = amount;
    return this;
  }

  @Override
  public String toString() {
    return toJson().encodePrettily();
  }

  public JsonObject toJson() {
    JsonObject result = new JsonObject()
      .put(FIELD_AMOUNT, amount)
      .put(FIELD_FROM, fromAccount)
      .put(FIELD_TO, toAccount);
    if (id != null) {
      result.put(FIELD_ID, id);
    }
    return result;
  }
}
