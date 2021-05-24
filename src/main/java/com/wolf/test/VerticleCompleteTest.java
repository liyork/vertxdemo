package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Description:
 * Created on 2021/4/17 11:27 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VerticleCompleteTest extends AbstractVerticle {

  //@Override
  //public void start() throws Exception {
  //  System.out.println(1111);
  //}

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    System.out.println(1111);
    //super.start(startPromise);
    //Thread.sleep(1111111);
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();// 仅有他并不能让进程退出
    Future<String> stringFuture = vertx.deployVerticle(VerticleCompleteTest.class.getName());// 能让进程不退出是因为下面代码
    stringFuture.onComplete(r -> {
      System.out.println(2222);
    });
    System.out.println(3333);

    // 源码如下
    //Transport transport = Transport.transport(false);
    //EventLoopGroup eventLoopGroup = transport.eventLoopGroup(Transport.IO_EVENT_LOOP_GROUP, 2, new ThreadFactory() {
    //  @Override
    //  public Thread newThread(Runnable r) {
    //    return new Thread(r);
    //  }
    //}, 1);
    //eventLoopGroup.execute(()->{
    //  System.out.println(111);
    //});
  }
}
