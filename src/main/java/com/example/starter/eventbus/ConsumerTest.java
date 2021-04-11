package com.example.starter.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Description:
 * Created on 2021/4/9 10:43 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ConsumerTest extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //对于每个Vertx来说，是单例的
    EventBus eventBus = vertx.eventBus();

    // 同一个地址可以注册许多不同的处理器，一个处理器也可以注册在多个不同的地址上。
    // 注册handlers,news.uk.sport表示消息的注册地址
    MessageConsumer<Object> consumer = eventBus.consumer("news.uk.sport");
    consumer.handler(message -> {
      System.out.println("1-I have received a message: " + message.body());
      message.reply("1-how interesting");// 回应
    });

    // 注册后的通知
    consumer.completionHandler(res -> {
      if (res.succeeded()) {
        System.out.println("1-The handler registration has reached all nodes");
      } else {
        System.out.println("Registration failed");
      }
    });

    // 撤销处理器
    //consumer.unregister(res -> {
    //  if (res.succeeded()) {
    //    System.out.println("The handler un-registration has reached all nodes");
    //  } else {
    //    System.out.println("Un-registration failed!");
    //  }
    //});


  }
}
