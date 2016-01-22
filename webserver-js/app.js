var Router = require("vertx-web-js/router");
var StaticHandler = require("vertx-web-js/static_handler");

var server = vertx.createHttpServer();
var router = Router.router(vertx);

var port = Vertx.currentContext().config().port;
if (port == null) {
  port = 8080;
}

var staticHandler = StaticHandler.create("./").setCachingEnabled(false)

router.get("/*").handler(staticHandler.handle);
server.requestHandler(router.accept).listen(8080, function(result, err) {
  if (err == null) {
    console.log("app running on http://localhost:" + port)
  } else {
    console.log("failed to start: " + err);
  }
});