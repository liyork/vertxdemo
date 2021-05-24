package com.wolf.test.mqtt.all;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

import java.util.concurrent.CountDownLatch;

/**
 * Description:
 * Created on 2021/5/13 10:30 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class MqttClientDemo extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    MqttClient client = MqttClient.create(vertx);

    context.executeBlocking(s -> {
      try {
        conn(client);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    super.start(startPromise);
  }

  private void conn(MqttClient client) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    client.connect(1883, "localhost", s -> {
      countDownLatch.countDown();
      //client.disconnect();
    });

    countDownLatch.await();
    System.out.println("isConnected:" + client.isConnected());

    // 接收服务端的发送消息
    client.publishHandler(s -> {
      System.out.println("There are new message in topic: " + s.topicName());
      System.out.println("Content(as string) of the message: " + s.payload().toString());
      System.out.println("QoS: " + s.qosLevel());
    });

    // 向服务器订阅主题
    client.subscribe("rpi2/temp", 2, sub -> {// 发送成功时调动
      System.out.println("subscribe rpi2/temp in client: " + sub.result());
    });

    // 向主题发布信息
    client.publish("temperature",
      Buffer.buffer("hello1111"),
      MqttQoS.AT_LEAST_ONCE,
      false,
      false);

    // by default you client keep connections with server automatically. That means that you don’t need to call ping in order to keep connections with server. The MqttClient will do it for you
    // options.setAutoKeepAlive(false);// 若想禁止则用false

    // when called each time publish is completed
    // could see the packetId of just received PUBACK or PUBCOMP packet
    client.publishCompletionHandler(id -> {// The handler WILL NOT BE CALLED if sent publish packet with QoS=0.
      System.out.println("Id of just received PUBACK or PUBCOMP packet is " + id);
    });
    // The line of code below will trigger publishCompletionHandler (QoS 2)
    client.publish("hello", Buffer.buffer("hello1"), MqttQoS.EXACTLY_ONCE, false, false);
    // The line of code below will trigger publishCompletionHandler (QoS is 1)
    client.publish("hello", Buffer.buffer("hello2"), MqttQoS.AT_LEAST_ONCE, false, false);
    // The line of code below does not trigger because QoS value is 0
    client.publish("hello", Buffer.buffer("hello3"), MqttQoS.AT_MOST_ONCE, false, false);

    // 订阅完成
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

    // 反订阅完成
    //client.unsubscribeCompletionHandler(id -> {
    //  System.out.println("Id of just received UNSUBACK packet is " + id);
    //});
    //client.subscribe("temp", 1);
    //client.unsubscribe("temp");

    client.subscribe("temp", 1);
    client.unsubscribe("temp", id -> {
      System.out.println("Id of just sent UNSUBSCRIBE packet is " + id);
    });

    // ping的结果
    client.pingResponseHandler(s -> {
      //The handler will be called time to time by default
      System.out.println("We have just received PINGRESP packet");
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("com.wolf.test.mqtt.all.MqttClientDemo");
  }
}
