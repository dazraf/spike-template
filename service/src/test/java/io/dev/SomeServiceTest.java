package io.dev;

import io.dev.someservice.api.SomeService;
import io.dev.someservice.api.Transaction;
import io.dev.someservice.impl.SomeServiceVerticle;
import io.dev.util.db.AbstractTestWithMongoDB;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.dev.util.testing.VertxMatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Unit test for simple App.
 */
@RunWith(VertxUnitRunner.class)
public class SomeServiceTest extends AbstractTestWithMongoDB {
  private static final double EPSILON = 0.000_000_000_1;

  private Vertx vertx;
  private SomeService someService;

  @Before
  public void setup(TestContext testContext) {
    VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(Integer.MAX_VALUE);
    this.vertx = Vertx.vertx(vertxOptions);
    Async async = testContext.async();

    JsonObject config = new JsonObject()
      .put(SomeService.CONFIG_MONGO_DB, new JsonObject()
        .put(SomeService.CONFIG_MONGO_DB_CONNECTION_STRING, getMongoDBConnectionString()));

    final DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);
    // deploy the service
    vertx.deployVerticle(new SomeServiceVerticle(), deploymentOptions, deploymentResult -> {
      testContext.assertTrue(deploymentResult.succeeded());

      // get a proxy
      this.someService = SomeService.createProxy(vertx);

      testContext.assertNotNull(this.someService);
      async.complete();
    });
  }

  @Test
  public void checkTransactionCanBeSavedAndGot(TestContext testContext) {
    int amount = 5;
    String from = "jim";
    String to = "sally";

    Async async = testContext.async();
    Transaction transaction = new Transaction(from, to, amount);
    someService.saveTransaction(transaction, saveResult -> {
      testContext.assertTrue(saveResult.succeeded());
      String id = saveResult.result();
      someService.getTransaction(id, readResult -> {
        testContext.assertTrue(readResult.succeeded());
        Transaction tx = readResult.result();

        assertThat(testContext, tx.getAmount(), closeTo(amount, EPSILON));
        assertThat(testContext, tx.getId(), equalTo(id));

        async.complete();
      });
    });
  }
}
