package io.dev.util.db;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractTestWithMongoDB {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTestWithMongoDB.class);
  private MongodProcess mongod;
  private int mongoDBPort;

  protected AbstractTestWithMongoDB() {
  }


  @Before
  public void createDB() throws IOException {
    LOG.info("Starting mongo db");
    IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION).build();

    MongodStarter runtime = MongodStarter.getDefaultInstance();

    MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
    this.mongod = mongodExecutable.start();

    this.mongoDBPort= mongodConfig.net().getPort();
    LOG.info("Started mongo db on {}", mongoDBPort);
  }

  @After
  public void stopDB() {
    if (mongod != null) {
      LOG.info("shutting down mongo db");
      mongod.stop();
      mongod = null;
      LOG.info("shutdown mongo db");
    }
  }

  protected String getMongoDBConnectionString() {
    return "mongodb://localhost:" + mongoDBPort;
  }
}
