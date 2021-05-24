package com.wolf.test.mqtt.simple;

/**
 * Description:
 * Created on 2021/5/14 5:22 PM
 *
 * @author 李超
 * @version 0.0.1
 */

import com.wolf.test.mqtt.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

/**
 * An example of using the MQTT server as a verticle
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Server.class);
  }

  @Override
  public void start() throws Exception {
    MqttServerOptions options = new MqttServerOptions()
      .setPort(1883)
      .setHost("0.0.0.0");

    MqttServer server = MqttServer.create(vertx, options);

    // 处理client的连接请求
    server.endpointHandler(endpoint -> {
      System.out.println("connected client " + endpoint.clientIdentifier());
      endpoint.publishHandler(message -> {
        System.out.println("Just received message on [" + message.topicName() + "] payload [" +
          message.payload() + "] with QoS [" +
          message.qosLevel() + "]");
      });

      endpoint.accept(false);
    });

    // 监听
    server.listen(ar -> {
      if (ar.succeeded()) {
        System.out.println("MQTT server started and listening on port " + server.actualPort());
      } else {
        System.err.println("MQTT server error on start" + ar.cause().getMessage());
      }
    });
  }
}
