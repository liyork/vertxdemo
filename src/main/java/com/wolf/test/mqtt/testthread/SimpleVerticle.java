package com.wolf.test.mqtt.testthread;

/**
 * Description: 测试线程使用
 * Created on 2021/5/14 5:22 PM
 *
 * @author 李超
 * @version 0.0.1
 */

import io.vertx.core.AbstractVerticle;

public class SimpleVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    System.out.println("test ....");
  }
}
