package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Description:
 * Created on 2021/4/11 9:39 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VerticleLifecycleTest extends AbstractVerticle {

  @Override
  public void init(Vertx vertx, Context context) {
    System.out.println("VerticleLifecycleTest init");
    super.init(vertx, context);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    System.out.println("VerticleLifecycleTest start " + Thread.currentThread().getName());
    super.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    System.out.println("VerticleLifecycleTest stop");
    super.stop(stopPromise);
  }

  public static void main(String[] args) throws InterruptedException {
    Vertx vertx = Vertx.vertx();
    AtomicReference<String> deployId = new AtomicReference<>();
    vertx.deployVerticle(VerticleLifecycleTest.class.getName(), event -> {
      System.out.println(event.getClass().getName() + "_" + event.result());
      deployId.set(event.result());
    });
    vertx.deployVerticle(VerticleLifecycleTest.class.getName());

    Thread.sleep(2000);

    String s = deployId.get();
    vertx.undeploy(s);
  }
}
