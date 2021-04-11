package com.wolf.test.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

/**
 * Description:
 * Created on 2021/4/9 10:49 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ProviderTest extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();

    // 发送给，所有注册在news.uk.sport上的handlers
    DeliveryOptions options = new DeliveryOptions();
    options.addHeader("some-header", "some-value");
    //EventBus publish = eventBus.publish("news.uk.sport", "Yay!Someone kicked a ball", options);

    // 发送给一个handler
    //eventBus.send("news.uk.sport", "Yay!Someone kicked a ball");

    // 请求有回应
    eventBus.request("news.uk.sport", "Yay! Someone kicked a ball across a patch of grass", ar -> {
      if (ar.succeeded()) {
        System.out.println("Received reply: " + ar.result().body());
      }
    });

    // 回应如何处理?
  }

  public static void main(String[] args) throws InterruptedException {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(ConsumerTest.class.getName());
    vertx.deployVerticle(ConsumerTest.class.getName());

    Thread.sleep(1000);
    vertx.deployVerticle(ProviderTest.class.getName());


  }
}
