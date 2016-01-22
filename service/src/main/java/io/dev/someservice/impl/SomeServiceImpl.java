package io.dev.someservice.impl;

import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneOptions;
import io.dev.someservice.api.SomeService;
import io.dev.someservice.api.Transaction;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * This is the actual body of the service
 */
class SomeServiceImpl implements SomeService {
  private static final String DB = "ExampleDB";
  private static final String TX_COLLECTION = "Transactions";
  private final MongoClient mongoClient;
  private final MongoCollection<Document> txCollection;

  public SomeServiceImpl(Vertx vertx, JsonObject config) {
    String connectionString = config.getJsonObject(CONFIG_MONGO_DB).getString(CONFIG_MONGO_DB_CONNECTION_STRING);
    this.mongoClient = MongoClients.create(connectionString);
    this.txCollection = mongoClient.getDatabase(DB).getCollection(TX_COLLECTION);
  }


  @Override
  public void saveTransaction(Transaction transaction, Handler<AsyncResult<String>> newIdHandler) {
    Document document = Document.parse(transaction.toString());
    txCollection.insertOne(document, (result, error) -> {
      if (error != null) {
        newIdHandler.handle(failedFuture(error));
      } else {
        String id = document.get("_id").toString();
        newIdHandler.handle(succeededFuture(id));
      }
    });
  }

  @Override
  public void getTransaction(String id, Handler<AsyncResult<Transaction>> transactionCallback) {
    try {
      final FindIterable<Document> cursor = txCollection.find(eq("_id", new ObjectId(id)));
      cursor.first((result, error) -> {
        if (error != null) {
          transactionCallback.handle(failedFuture(error));
        } else {
          Transaction tx = new Transaction(new JsonObject(result.toJson()));
          transactionCallback.handle(succeededFuture(tx));
        }
      });
    } catch (Throwable throwable) {
      transactionCallback.handle(failedFuture(throwable));
    }
  }

  @Override
  public void close() {
    if (mongoClient != null) {
      this.mongoClient.close();
    }
  }
}
