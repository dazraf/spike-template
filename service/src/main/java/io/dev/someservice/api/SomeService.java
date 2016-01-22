package io.dev.someservice.api;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * The service interface
 */
@ProxyGen // used to generate java proxies
@VertxGen // used to generate cross-language proxies
public interface SomeService {
  String CONFIG_MONGO_DB = "mongodb"; // config object name
  String CONFIG_MONGO_DB_CONNECTION_STRING = "connection-string"; // connection config name

  void saveTransaction(Transaction transaction, Handler<AsyncResult<String>> newIdHandler);

  // The service methods
  void getTransaction(String id, Handler<AsyncResult<Transaction>> transactionHandler);

  // Other methods go here

  // to close this service - this is an optional - but we need this to close the db connection
  @ProxyClose
  void close();

  // Factory methods
  // get a proxy to this service
  static SomeService createProxy(Vertx vertx) {
    return ProxyHelper.createProxy(SomeService.class, vertx, SomeService.class.getCanonicalName());
  }
}
