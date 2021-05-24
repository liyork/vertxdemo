package com.wolf.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Created on 2021/4/17 6:12 PM
 *
 * @author 李超
 * @version 0.0.1
 */
@ExtendWith(VertxExtension.class)
public class Junit5BaseTest {

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> {
      HttpClient client = vertx.createHttpClient();
      client.request(HttpMethod.GET, 8080, "localhost", "/")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assert buffer.toString().equals("Plop");
          testContext.completeNow();
        })));
    }));
  }
}
