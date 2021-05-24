package com.wolf.test.mqtt.ssl;

import com.wolf.test.mqtt.Runner;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

/**
 * Description:
 * Created on 2021/5/14 5:43 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Client extends AbstractVerticle {

  private static final String MQTT_TOPIC = "/my_topic";
  private static final String MQTT_MESSAGE = "Hello Vert.x MQTT Client";
  private static final String BROKER_HOST = "localhost";
  private static final int BROKER_PORT = 8883;

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Client.class);
  }

  @Override
  public void start() throws Exception {
    MqttClientOptions options = new MqttClientOptions();
    options.setSsl(true);
    options.setTrustAll(true);

    MqttClient mqttClient = MqttClient.create(vertx, options);

    mqttClient.connect(BROKER_PORT, BROKER_HOST, ch -> {
      if (ch.succeeded()) {
        System.out.println("Connected to a server");

        mqttClient.publish(
          MQTT_TOPIC,
          Buffer.buffer(MQTT_MESSAGE),
          MqttQoS.AT_MOST_ONCE,
          false,
          false,
          s -> mqttClient.disconnect(d -> System.out.println("Disconnected from server")));
      } else {
        System.out.println("Failed to connect to a server");
        System.out.println(ch.cause());
      }
    });
  }
}
