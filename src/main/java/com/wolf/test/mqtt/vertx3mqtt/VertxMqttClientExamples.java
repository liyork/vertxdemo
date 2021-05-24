package com.wolf.test.mqtt.vertx3mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

/**
 * Description:
 * Created on 2021/5/14 5:54 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VertxMqttClientExamples {

  /**
   * Example for demonstration of how {@link MqttClient#connect(int, String, Handler)} and  {@link MqttClient#disconnect()} methods
   * should be used
   *
   * @param vertx
   */
  public void example1(Vertx vertx) {
    MqttClient client = MqttClient.create(vertx);

    client.connect(1883, "mqtt.eclipse.org", s -> {
      client.disconnect();
    });
  }

  /**
   * Example for handling publish messages from server
   * 处理服务端的publish消息
   *
   * @param client
   */
  public void example2(MqttClient client) {
    client.publishHandler(s -> {// 接收服务端的发布
      System.out.println("There are new message in topic: " + s.topicName());
      System.out.println("Content(as string) of the message: " + s.payload().toString());
      System.out.println("QoS: " + s.qosLevel());
    }).subscribe("rpi2/temp", 2);
  }

  /**
   * Example for sending publish message
   *
   * @param client
   */
  public void example3(MqttClient client) {
    // 发布消息
    client.publish("temperature",
      Buffer.buffer("hello"),
      MqttQoS.AT_LEAST_ONCE,
      false,
      false);
  }

  /**
   * Example for disabling keepAlive feature
   * In order to keep connection with server you should time to time send something to server otherwise server will close the connection.
   * The right way to keep connection alive is a ping() method
   *
   * @param options
   */
  public void example4(MqttClientOptions options) {
    options.setAutoKeepAlive(false);// 禁止
  }

  /**
   * Example for publishCompletionHandler method demonstration
   * 对应关系：
   * EXACTLY_ONCE--publishReceived
   * AT_LEAST_ONCE--publishAcknowledge
   *
   * @param client
   */
  public void example5(MqttClient client) {
    client.publishCompletionHandler(id -> {// 发布完成时回调，即服务端PUBACK or PUBCOMP
      System.out.println("Id of just received PUBACK or PUBCOMP packet is " + id);
    });
    // The line of code below will trigger publishCompletionHandler (QoS 2)
    client.publish("hello", Buffer.buffer("hello"), MqttQoS.EXACTLY_ONCE, false, false);
    // The line of code below will trigger publishCompletionHandler (QoS is 1)
    client.publish("hello", Buffer.buffer("hello"), MqttQoS.AT_LEAST_ONCE, false, false);
    // The line of code below does not trigger because QoS value is 0
    client.publish("hello", Buffer.buffer("hello"), MqttQoS.AT_MOST_ONCE, false, false);
  }

  /**
   * Example for subscribeCompletionHandler method demonstration
   *
   * @param client
   */
  public void example6(MqttClient client) {
    client.subscribeCompletionHandler(mqttSubAckMessage -> {
      System.out.println("Id of just received SUBACK packet is " + mqttSubAckMessage.messageId());
      for (int s : mqttSubAckMessage.grantedQoSLevels()) {
        if (s == 0x80) {
          System.out.println("Failure");
        } else {
          System.out.println("Success. Maximum QoS is " + s);
        }
      }
    });
    client.subscribe("temp", 1);
    client.subscribe("temp2", 2);
  }

  /**
   * Example for unsubscribeCompletionHandler method demonstration
   *
   * @param client
   */
  public void example7(MqttClient client) {
    client.unsubscribeCompletionHandler(id -> {
      System.out.println("Id of just received UNSUBACK packet is " + id);
    });
    client.subscribe("temp", 1);
    client.unsubscribe("temp");
  }

  /**
   * Example for unsubscribe method demonstration
   *
   * @param client
   */
  public void example8(MqttClient client) {
    client.subscribe("temp", 1);
    client.unsubscribe("temp", id -> {
      System.out.println("Id of just sent UNSUBSCRIBE packet is " + id);
    });
  }

  /**
   * Example for pingResponseHandler method demonstration
   *
   * @param client
   */
  public void example9(MqttClient client) {
    client.pingResponseHandler(s -> {
      //The handler will be called time to time by default
      System.out.println("We have just received PINGRESP packet");
    });
  }
}
