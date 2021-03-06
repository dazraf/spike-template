package io.dev;

import io.dev.someservice.api.SomeService;
import io.dev.someservice.api.Transaction;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.dev.util.CorsFactory.getCorsHandler;
import static java.lang.Integer.parseInt;

/**
 * Hello world!
 */
public class App extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);
  private SomeService someService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    int port = config().getInteger("port", 8080);

    this.someService = SomeService.createProxy(vertx);
    Router router = Router.router(vertx);
    CorsHandler corsHandler = getCorsHandler();
    router.route().handler(corsHandler);

    setupWebService(router);
    setupStatic(router);

    vertx.createHttpServer().requestHandler(router::accept).listen(port, ar -> {
      if (ar.succeeded()) {
        LOG.info("Server started on http://localhost:{}", port);
        startFuture.complete();
      } else {
        LOG.error("failed to start server", ar.cause());
        startFuture.fail(ar.cause());
      }
    });
  }

  private void setupStatic(Router router) {
    // redirect root to index.html
    router.get("/").handler(rc -> {
      rc.response()
        .setStatusCode(301)
        .putHeader("Location", "/index.html")
        .end();
    });
    router.get("/lib/*").handler(createStatic("web/node_modules"));
    router.get("/*").handler(createStatic("web"));
  }

  public StaticHandler createStatic(String filePath) {
    return StaticHandler.create(filePath);
  }

  public void setupWebService(Router router) {
    router.post("/api/transaction").handler(rc -> {
      rc.request().bodyHandler(body -> {
        final JsonObject bodyAsJson = body.toJsonObject();
        saveTransaction(bodyAsJson, rc.response());
      });
    });

    router.get("/api/transaction/:id").handler(rc -> {
      String id = rc.request().getParam("id");
      retrieveTransaction(id, rc.response());
    });
  }

  private void saveTransaction(JsonObject bodyAsJson, HttpServerResponse response) {
    someService.saveTransaction(new Transaction(bodyAsJson), saveResult -> {
      response
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("id", saveResult.result()).toString());
    });
  }

  private void retrieveTransaction(String id, HttpServerResponse response) {
    someService.getTransaction(id, tx -> {
      if (tx.failed()) {
        response
          .setStatusCode(500)
          .setStatusMessage(tx.cause().getMessage());
      } else {
        response
          .putHeader("Content-Type", "application/json")
          .end(tx.result().toString());
      }
    });
  }
}
