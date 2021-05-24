package com.wolf.test;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Created on 2021/4/17 6:04 PM
 *
 * @author 李超
 * @version 0.0.1
 */
@ExtendWith(VertxExtension.class)
public class BaseAsynTest {
  Vertx vertx = Vertx.vertx();

  @Test
  void start_http_server() throws Throwable {
    VertxTestContext testContext = new VertxTestContext();

    vertx.createHttpServer()
      .requestHandler(req -> req.response().end())
      .listen(16969)
      .onComplete(testContext.succeedingThenComplete());

    assert testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
