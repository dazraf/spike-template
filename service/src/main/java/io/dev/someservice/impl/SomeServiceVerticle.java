package io.dev.someservice.impl;

import io.dev.someservice.api.SomeService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The verticle that deploys the service
 */
public class SomeServiceVerticle extends AbstractVerticle {
  private static Logger LOG = LoggerFactory.getLogger(SomeServiceVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    try {
      SomeService service = new SomeServiceImpl(vertx, config());
      // we use the FQN of the SomeService interface as the 'address' of the service
      ProxyHelper.registerService(SomeService.class, vertx, service, SomeService.class.getCanonicalName());
      startFuture.complete();
    } catch (Throwable throwable) {
      LOG.error("Failed to deploy SomeService service", throwable);
      startFuture.fail(throwable);
    }
  }
}
