package com.wolf.test.mqtt.ssl;

import com.wolf.test.mqtt.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

/**
 * Description:
 * Created on 2021/5/14 5:42 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Server.class);
  }

  @Override
  public void start() throws Exception {
    MqttServerOptions options = new MqttServerOptions()
      .setPort(8883)
      .setPemKeyCertOptions(new PemKeyCertOptions()
        .setKeyPath("server-key.pem")
        .setCertPath("server-cert.pem"))
      .setSsl(true);

    MqttServer mqttServer = MqttServer.create(vertx, options);

    mqttServer.endpointHandler(endpoint -> {
      // shows main connect info
      System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, " +
        "clean session = " + endpoint.isCleanSession());

      // accept connection from the remote client
      endpoint.accept(false);

    }).listen(ar -> {
      if (ar.succeeded()) {
        System.out.println("MQTT server is listening on port " + mqttServer.actualPort());
      } else {
        System.err.println("Error on starting the server" + ar.cause().getMessage());
      }
    });
  }
}
