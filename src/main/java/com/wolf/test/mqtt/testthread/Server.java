package com.wolf.test.mqtt.testthread;

/**
 * Description: 测试线程使用
 * Created on 2021/5/14 5:22 PM
 *
 * @author 李超
 * @version 0.0.1
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

public class Server extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(Server.class.getName());
    vertx.deployVerticle(SimpleVerticle.class.getName());
  }

  @Override
  public void start() throws Exception {
    System.out.println(Thread.currentThread().getName() + " in start");
    MqttServerOptions options = new MqttServerOptions()
      .setPort(1883)
      .setHost("0.0.0.0");

    MqttServer server = MqttServer.create(vertx, options);

    server.endpointHandler(endpoint -> {
      System.out.println(Thread.currentThread().getName() + " in endpointHandler");
      System.out.println("connected client " + endpoint.clientIdentifier());
      endpoint.publishHandler(message -> {
        System.out.println(Thread.currentThread().getName() + " in publishHandler");
        System.out.println("Just received message on [" + message.topicName() + "] payload [" +
          message.payload() + "] with QoS [" +
          message.qosLevel() + "]");
      });

      endpoint.accept(false);
    });

    server.listen(ar -> {
      System.out.println(Thread.currentThread().getName() + " in listen");
      if (ar.succeeded()) {
        System.out.println("MQTT server started and listening on port " + server.actualPort());
      } else {
        System.err.println("MQTT server error on start" + ar.cause().getMessage());
      }
    });
  }
}
