package com.wolf.test.mqtt.all;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created on 2021/5/13 9:43 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class MqttServerDemo extends AbstractVerticle {
  // If you’re creating MQTT servers from inside verticles, those servers will be automatically closed when the verticle is undeployed
  @Override
  public void start() throws Exception {
    MqttServerOptions options = new MqttServerOptions();
    // SSL/TLS
    //.setPort(8883)
    //.setKeyCertOptions(new PemKeyCertOptions()
    //  .setKeyPath("./src/test/resources/tls/server-key.pem")
    //  .setCertPath("./src/test/resources/tls/server-cert.pem"))
    //.setSsl(true);

    //options.setUseWebSocket(true);// listen for websocket connections on the path /mqtt.

    MqttServer mqttServer = MqttServer.create(vertx, options);
    System.out.println(mqttServer);
    // specify the handler called when a remote client sends a CONNECT message for connecting to the server
    mqttServer.endpointHandler(endpoint -> {
      // shows main connect info
      System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());
      if (endpoint.auth() != null) {
        System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
      }
      if (endpoint.will() != null) {
        String msg = null;
        byte[] willMessageBytes = endpoint.will().getWillMessageBytes();
        if (null != willMessageBytes) {
          msg = new String(willMessageBytes);
        }
        System.out.println("[will topic = " + endpoint.will().getWillTopic() + " msg = " + msg +
          " QoS = " + endpoint.will().getWillQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
      }

      System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

      // accept connection from the remote client
      // replying to the remote client with the corresponding CONNACK message
      endpoint.accept(false);
      // the connection is established

      // when the remote client sends a DISCONNECT message
      endpoint.disconnectHandler(v -> {
        System.out.println("Received disconnect from client");
      });

      // for the incoming subscription request
      endpoint.subscribeHandler(subscribe -> {
        List<MqttQoS> grantedQosLevels = new ArrayList<>();
        for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
          System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
          grantedQosLevels.add(s.qualityOfService());
        }
        // ack the subscriptions request
        endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
      });

      // when the client sends an UNSUBSCRIBE message
      endpoint.unsubscribeHandler(unsubscribe -> {
        for (String t : unsubscribe.topics()) {
          System.out.println("Unsubscription for " + t);
        }
        // ack the subscriptions request
        // replying to the client with the related UNSUBACK message
        endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
      });

      // 处理接收到的发布信息
      // when the client sends a PUBLISH message
      endpoint.publishHandler(message -> {
        System.out.println("Just received message [" + message.topicName() + "_" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "]");
        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
          endpoint.publishAcknowledge(message.messageId());// reply with a PUBACK message
        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
          endpoint.publishReceived(message.messageId());// reply with a PUBREC message
        } else {
          // AT_MOST_ONCE,no need from the endpoint to reply the client
        }
      });

      // the same endpoint should handle the PUBREL message received from the client, the remote client sends it after receiving the PUBREC from the endpoint
      endpoint.publishReleaseHandler(messageId -> {
        // to close the QoS level 2 delivery,sending the PUBCOMP message to the client
        endpoint.publishComplete(messageId);
      });

      // 主动发布
      // sending a PUBLISH message
      //endpoint.publish("my_topic",
      //  Buffer.buffer("Hello from the Vert.x MQTT server"),
      //  MqttQoS.EXACTLY_ONCE,
      //  false,
      //  false);

      // specifing handlers for handling QoS 1 and 2
      // if AT_MOST_ONCE,the endpoint won’t receiving any feedback from the client.

      endpoint.publishAcknowledgeHandler(messageId -> {// if AT_LEAST_ONCE,the endpoint needs to handle the PUBACK message received from the client in order to receive final acknowledge of delivery
        System.out.println("Received ack for message = " + messageId);
      }).publishReceivedHandler(messageId -> {// EXACTLY_ONCE,handle the PUBREC message received from the client
        endpoint.publishRelease(messageId);// replying to the client with the PUBREL message.
      }).publishCompletionHandler(messageId -> {// The last step is to handle the PUBCOMP message received from the client as final acknowledge for the published message,when the final PUBCOMP message is received.
        System.out.println("Received ack for message = " + messageId);
      });

      // When the CONNECT message is received, the server takes care of the keep alive timeout specified inside that message in order to check if the client doesn’t send messages in such timeout
      // At same time, for every PINGREQ received, the server replies with the related PINGRESP.
      endpoint.pingHandler(v -> {// when a PINGREQ message is received from the client
        // It’s just a notification to the application that the client isn’t sending meaningful messages but only pings for keeping alive;
        // in any case the PINGRESP is automatically sent by the server internally as described above.
        System.out.println("Ping received from client");
      });

      new Thread(() -> {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        endpoint.publish("xxxx", Buffer.buffer("xxxxx123"), MqttQoS.AT_LEAST_ONCE, false, false);
      }).start();

    }).listen(ar -> {// 开始监听
      if (ar.succeeded()) {
        System.out.println("MQTT server is listening on port " + ar.result().actualPort());
      } else {
        System.out.println("Error on starting the server");
        ar.cause().printStackTrace();
      }
    });

    // stops to listen for incoming connections and closes all the active connections with remote clients.
    // 异步
    //mqttServer.close(v -> {// when the server is really closed
    //  System.out.println("MQTT server closed");
    //});

    super.start();
  }

  public static void main(String[] args) {
    DeploymentOptions options = new DeploymentOptions();
    // The handlers related to the MQTT server are always executed in the same event loop thread
    // in order to use more cores, it’s possible to deploy more instances of the MQTT server.
    //options.setInstances(2);// 多个verticle，每个拉起来一个mqttServer
    // even only MQTT server is deployed but as incoming connections arrive, Vert.x distributes them in a round-robin fashion to any of the connect handlers executed on different cores.
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("com.wolf.test.mqtt.all.MqttServerDemo", options);
  }
}
