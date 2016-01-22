package io.dev.util;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * Simple factory to setup CORS
 */
public class CorsFactory {
  public static CorsHandler getCorsHandler() {
    return CorsHandler.create("*")
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedMethod(HttpMethod.GET)
      // add more here
      .allowedHeader("Content-Type")
      .allowedHeader("Authorization")
      .allowedHeader("www-authenticate")
      .allowedHeader("Content-Length")
      .allowedHeader("Location");
  }
}
