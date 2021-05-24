package com.wolf.test;

import io.vertx.core.AbstractVerticle;

public class HttpServerVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Plop");
    }).listen(8080);
  }
}
