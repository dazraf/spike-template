package io.dev.someservice.impl;

import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import io.dev.someservice.api.SomeService;
import io.dev.someservice.api.Transaction;
import io.dev.util.db.MongoClientCreator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.impl.codec.json.JsonObjectCodec;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
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
  private static final String FIELD_MONGO_ID = "_id";

  private final MongoClient mongoClient;
  private final MongoCollection<JsonObject> txCollection;

  public SomeServiceImpl(Vertx vertx, JsonObject config) {
    String connectionString = config.getJsonObject(CONFIG_MONGO_DB).getString(CONFIG_MONGO_DB_CONNECTION_STRING);
    this.mongoClient = MongoClientCreator.createMongoClient(connectionString);
    this.txCollection = mongoClient.getDatabase(DB).getCollection(TX_COLLECTION, JsonObject.class);
  }


  @Override
  public void saveTransaction(Transaction transaction, Handler<AsyncResult<String>> newIdHandler) {
    final JsonObject jsonObject = transaction.toJson();
    txCollection.insertOne(jsonObject, (result, error) -> {
      if (error != null) {
        newIdHandler.handle(failedFuture(error));
      } else {
        String id = jsonObject.getString(FIELD_MONGO_ID);
        newIdHandler.handle(succeededFuture(id));
      }
    });
  }

  @Override
  public void getTransaction(String id, Handler<AsyncResult<Transaction>> transactionCallback) {
    try {
      final FindIterable<JsonObject> cursor = txCollection.find(eq("_id", id));
      cursor.first((result, error) -> {
        if (error != null) {
          transactionCallback.handle(failedFuture(error));
        } else {
          Transaction tx = new Transaction(result);
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
