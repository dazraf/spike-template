package io.dev.util.db;

import com.mongodb.ConnectionString;
import com.mongodb.WriteConcern;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.impl.codec.json.JsonObjectCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * This class both simplifies the creation of the mongo client and
 * also allows us to use the mongo async api directly with JsonObjects
 */
public class MongoClientCreator {
  public static MongoClient createMongoClient(String connectionString) {
    final JsonObjectCodec jsonObjectCode = new JsonObjectCodec(new JsonObject().put("useObjectId", false));

    CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
      MongoClients.getDefaultCodecRegistry(),
      CodecRegistries.fromCodecs(jsonObjectCode)
    );

    final MongoClientSettings clientSettings = MongoClientSettings.builder()
      .codecRegistry(codecRegistry)
      .writeConcern(WriteConcern.ACKNOWLEDGED)
      .clusterSettings(ClusterSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .build())
      .build();

    return MongoClients.create(clientSettings);
  }
}
