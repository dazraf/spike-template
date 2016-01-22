package io.dev;

import io.dev.someservice.api.SomeService;
import io.dev.someservice.impl.SomeServiceVerticle;
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
public class AppTest {
  private static final int SERVICE_PORT = 8080;
  private static final double EPSILON = 0.000_000_000_1;
  private Vertx vertx;
  private SomeService someService;

  @Before
  public void setup(TestContext testContext) {
    this.vertx = Vertx.vertx();
    Async async = testContext.async();

    // deploy the service
    vertx.deployVerticle(new SomeServiceVerticle(), deployService -> {
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

    int ID = 1;
    httpClient.get("/api/transaction/" + ID).handler(clientResponse -> {
      assertThat(testContext, clientResponse.statusCode(), allOf(greaterThanOrEqualTo(200), lessThan(300)));

      clientResponse.bodyHandler(buffer -> {
        final JsonObject jsonObject = new JsonObject(buffer.toString());
        int id = jsonObject.getInteger("id");
        double amount = jsonObject.getDouble("amount");
        assertThat(testContext, id, equalTo(ID));
        assertThat(testContext, amount, closeTo(10, EPSILON));
        async.complete();
      });
    }).end();
  }
}
