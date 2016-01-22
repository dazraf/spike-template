package io.dev.someservice.impl;

import io.dev.someservice.api.SomeService;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * The verticle that deploys the service
 */
public class SomeServiceVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {

    SomeService service = new SomeServiceImpl(vertx, config());
    // we use the FQN of the SomeService interface as the 'address' of the service
    ProxyHelper.registerService(SomeService.class, vertx, service, SomeService.class.getCanonicalName());
  }
}
