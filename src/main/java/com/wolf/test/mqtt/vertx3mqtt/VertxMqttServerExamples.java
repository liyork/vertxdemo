package com.wolf.test.mqtt.vertx3mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created on 2021/5/14 5:53 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VertxMqttServerExamples {

  /**
   * Example for handling client connection
   * 监听并处理客户第一次连接
   *
   * @param vertx
   */
  public void example1(Vertx vertx) {

    MqttServer mqttServer = MqttServer.create(vertx);
    // when a remote client sends a CONNECT message for connecting to the server
    mqttServer.endpointHandler(endpoint -> {
      // shows main connect info
      System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

      if (endpoint.auth() != null) {
        System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
      }
      if (endpoint.will() != null) {
        System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
          " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
      }

      System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

      // replying to the remote client with the corresponding CONNACK message,in this way, the connection is established
      // accept connection from the remote client
      endpoint.accept(false);
    }).listen(ar -> {// 监听
      if (ar.succeeded()) {
        System.out.println("MQTT server is listening on port " + ar.result().actualPort());
      } else {
        System.out.println("Error on starting the server");
        ar.cause().printStackTrace();
      }
    });
  }

  /**
   * Example for handling client disconnection
   * 失连
   *
   * @param endpoint
   */
  public void example2(MqttEndpoint endpoint) {
    // when the remote client sends a DISCONNECT message in order to disconnect from the server
    // handling disconnect message
    endpoint.disconnectHandler(v -> {
      System.out.println("Received disconnect from client");
    });
  }

  /**
   * Example for handling client connection using SSL/TLS
   *
   * @param vertx
   */
  public void example3(Vertx vertx) {
    MqttServerOptions options = new MqttServerOptions()
      .setPort(8883)
      .setKeyCertOptions(new PemKeyCertOptions()
        .setKeyPath("./src/test/resources/tls/server-key.pem")
        .setCertPath("./src/test/resources/tls/server-cert.pem"))
      .setSsl(true);

    MqttServer mqttServer = MqttServer.create(vertx, options);
    mqttServer.endpointHandler(endpoint -> {

      // shows main connect info
      System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

      if (endpoint.auth() != null) {
        System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
      }
      if (endpoint.will() != null) {
        System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + new String(endpoint.will().getWillMessageBytes()) +
          " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
      }

      System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

      // accept connection from the remote client
      endpoint.accept(false);

    }).listen(ar -> {
      if (ar.succeeded()) {
        System.out.println("MQTT server is listening on port " + ar.result().actualPort());
      } else {
        System.out.println("Error on starting the server");
        ar.cause().printStackTrace();
      }
    });
  }

  /**
   * Example for handling client subscription request
   *
   * @param endpoint
   */
  public void example4(MqttEndpoint endpoint) {
    // handling requests for subscriptions
    endpoint.subscribeHandler(subscribe -> {
      List<MqttQoS> grantedQosLevels = new ArrayList<>();
      for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
        System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
        grantedQosLevels.add(s.qualityOfService());
      }
      // replying to the client with the related SUBACK message containing the granted QoS levels
      // ack the subscriptions request
      endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
    });
  }

  /**
   * Example for handling client unsubscription request
   *
   * @param endpoint
   */
  public void example5(MqttEndpoint endpoint) {
    // replying to the client with the related UNSUBACK message.
    // handling requests for unsubscriptions
    endpoint.unsubscribeHandler(unsubscribe -> {
      for (String t : unsubscribe.topics()) {
        System.out.println("Unsubscription for " + t);
      }
      // ack the subscriptions request
      endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
    });
  }

  /**
   * Example for handling client published message
   * 发布消息有等级
   * <p>
   * EXACTLY_ONCE的流程：
   * <-- publish MqttQoS.EXACTLY_ONCE
   * publishReceived PUBREC -->
   * <-- PUBREL
   * publishComplete PUBCOMP -->
   *
   * @param endpoint
   */
  public void example6(MqttEndpoint endpoint) {
    // when the client sends a PUBLISH message.
    // handling incoming published messages
    endpoint.publishHandler(message -> {
      System.out.println("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");
      if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {// needs to reply with a PUBACK message using
        endpoint.publishAcknowledge(message.messageId());
      } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {// needs to reply with a PUBREC message using the available
        // the same endpoint should handle the PUBREL message received from the client as well (the remote client sends it after receiving the PUBREC from the endpoint) and it can do that specifying the handler through the publishReleaseHandler method
        endpoint.publishReceived(message.messageId());
      } else {// MqttQoS.AT_MOST_ONCE,no need from the endpoint to reply the client
      }
    }).publishReleaseHandler(messageId -> {
      endpoint.publishComplete(messageId);
    });
  }


  /**
   * Example for handling publish message to the client
   * 服务端发布消息给客户
   *
   * @param endpoint
   */
  public void example7(MqttEndpoint endpoint) {
    // just as example, publish a message with QoS level 2
    endpoint.publish("my_topic",
      Buffer.buffer("Hello from the Vert.x MQTT server"),
      MqttQoS.EXACTLY_ONCE,
      false,
      false);

    // AT_MOST_ONCE, the endpoint won’t receiving any feedback from the client.
    // specifing handlers for handling QoS 1 and 2
    endpoint.publishAcknowledgeHandler(messageId -> {// AT_LEAST_ONCE需要处理PUBACK，结束本次至少一次
      System.out.println("Received ack for message = " + messageId);
    }).publishReceivedHandler(messageId -> {// EXACTLY_ONCE, needs to handle the PUBREC
      endpoint.publishRelease(messageId);// replying to the client with the PUBREL message
    }).publishCompletionHandler(messageId -> {// 处理PUBCOMP message
      System.out.println("Received ack for message = " + messageId);
    });
  }

  /**
   * Example for being notified by client keep alive
   * When the CONNECT message is received, the server 看消息中的timeout，check if the client doesn’t send messages in such timeout.
   * At same time, for every PINGREQ received, the server replies with the related PINGRESP.
   *
   * @param endpoint
   */
  public void example8(MqttEndpoint endpoint) {
    // handling ping from client
    endpoint.pingHandler(v -> {// It’s just a notification to the application that the client isn’t sending meaningful messages but only pings for keeping alive
      System.out.println("Ping received from client");// PINGRESP is automatically sent
    });
  }

  /**
   * Example for closing the server
   *
   * @param mqttServer
   */
  public void example9(MqttServer mqttServer) {
    mqttServer.close(v -> {// stops to listen for incoming connections and closes all the active connections with remote clients
      System.out.println("MQTT server closed");
    });
  }

  // creating MQTT servers from inside verticles, those servers will be automatically closed when the verticle is undeployed

  /**
   * Example for scaling (sharing MQTT servers)
   *
   * @param vertx
   */
  public void example10(Vertx vertx) {
    for (int i = 0; i < 10; i++) {
      MqttServer mqttServer = MqttServer.create(vertx);
      mqttServer.endpointHandler(endpoint -> {
        // handling endpoint
      }).listen(ar -> {
        // handling start listening
      });
    }
  }

  /**
   * Example for scaling (sharing MQTT servers)
   * The handlers related to the MQTT server are always executed in the same event loop thread.
   * In order to use more cores, it’s possible to deploy more instances of the MQTT server.
   *
   * @param vertx
   */
  public void example11(Vertx vertx) {
    DeploymentOptions options = new DeploymentOptions().setInstances(10);
    vertx.deployVerticle("com.mycompany.MyVerticle", options);
  }

  /**
   * Example for serving websocket connections
   *
   * @param vertx
   */
  public void example12(Vertx vertx) {
    MqttServerOptions options = new MqttServerOptions()
      // listen for websocket connections on the path /mqtt
      .setUseWebSocket(true);

    MqttServer mqttServer = MqttServer.create(vertx, options);
  }
}
