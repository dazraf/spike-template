package io.dev;

import io.dev.someservice.api.SomeService;
import io.dev.someservice.api.Transaction;
import io.dev.someservice.impl.SomeServiceVerticle;
import io.dev.util.db.AbstractTestWithMongoDB;
import io.dev.util.testing.VertxMatcherAssert;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.dev.util.testing.VertxMatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Unit test for simple App.
 */
@RunWith(VertxUnitRunner.class)
public class AppTest extends AbstractTestWithMongoDB {
  private static final int SERVICE_PORT = 8080;
  private static final double EPSILON = 0.000_000_000_1;
  private Vertx vertx;
  private SomeService someService;

  @Before
  public void setup(TestContext testContext) {
    this.vertx = Vertx.vertx();
    Async async = testContext.async();

    JsonObject config = new JsonObject()
      .put(SomeService.CONFIG_MONGO_DB, new JsonObject()
        .put(SomeService.CONFIG_MONGO_DB_CONNECTION_STRING, getMongoDBConnectionString()));

    final DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);

    // deploy the service
    vertx.deployVerticle(new SomeServiceVerticle(), deploymentOptions, deployService -> {
      testContext.assertTrue(deployService.succeeded());
      vertx.deployVerticle(new App(), new DeploymentOptions().setConfig(new JsonObject().put("port", SERVICE_PORT)), deployApp -> {
        testContext.assertTrue(deployApp.succeeded());
        async.complete();
      });
    });
  }

  @Test
  public void checkTransactionCanBeGot(TestContext testContext) {
    Async async = testContext.async();
    final HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(SERVICE_PORT));

    double AMOUNT = 5.5;
    Transaction transaction = new Transaction("jim", "sally", AMOUNT);
    httpClient.post("/api/transaction/").handler(postResponse -> {
      // POST handler
      postResponse.bodyHandler(POST_body -> {
        // retrieve body and get ID
        String saved_ID = new JsonObject(POST_body.toString()).getString("id");

        // now try to retrieve it
        httpClient.get("/api/transaction/" + saved_ID)
          .handler(clientResponse -> {
            // GET handler
            assertThat(testContext, clientResponse.statusCode(), allOf(greaterThanOrEqualTo(200), lessThan(300)));
            clientResponse.bodyHandler(GET_body -> {
              // retrieve body
              final JsonObject jsonObject = GET_body.toJsonObject();

              // check fields are correct
              String retrieved_ID = jsonObject.getString("_id");
              double amount = jsonObject.getDouble("amount");
              assertThat(testContext, retrieved_ID, equalTo(saved_ID));
              assertThat(testContext, amount, closeTo(AMOUNT, EPSILON));
              async.complete();
            });
          })
          .end();
      });
    })
      .end(transaction.toJson().toString());
  }
}
