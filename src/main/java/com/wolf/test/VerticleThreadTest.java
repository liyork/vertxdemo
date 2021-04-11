package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * Description:
 * Vert.x保证同一个普通Verticle（即EventLoop Verticle，非Worker Verticle）内部的所有处理器（Handler）都只会由同一个EventLoop线程调用，
 * 由此保证Verticle内部的线程安全
 * Verticle内部是thread safe/线程安全的，Verticle之间传递的数据是immutable/不可改变的。
 * Created on 2021/4/11 2:24 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VerticleThreadTest extends AbstractVerticle {
  int i = 0;//属性变量

  public void start() throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      i++;
      System.out.println(Thread.currentThread().getName() + " in 8080");
      req.response().end();//要关闭请求，否则连接很快会被占满
    }).listen(8080);

    vertx.createHttpServer().requestHandler(req -> {
      System.out.println(Thread.currentThread().getName() + " in 8081");
      req.response().end("" + i);
    }).listen(8081);
  }

  // curl localhost:8080
  // curl localhost:8081
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(VerticleThreadTest.class.getName());
  }
}

