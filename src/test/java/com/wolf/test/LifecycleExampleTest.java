package com.wolf.test;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Description:
 * Since these objects help waiting for asynchronous operations to complete,
 * a new instance is created for any @Test, @BeforeAll, @BeforeEach, @AfterEach and @AfterAll method.
 * Created on 2021/4/17 6:19 PM
 *
 * @author 李超
 * @version 0.0.1
 */
@ExtendWith(VertxExtension.class)
class LifecycleExampleTest {

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new HttpServerVerticle(), testContext.succeedingThenComplete());
    System.out.println("Deploy a verticle");
  }

  @Test
  @DisplayName("A first test")
  void foo(Vertx vertx, VertxTestContext testContext) {
    // (...)
    System.out.println("A first test");
    testContext.completeNow();
  }

  @Test
  @DisplayName("A second test")
  void bar(Vertx vertx, VertxTestContext testContext) {
    // (...)
    System.out.println("A second test");
    testContext.completeNow();
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assert vertx.deploymentIDs().size() == 1;
    System.out.println("Check that the verticle is still there");
  }
}
