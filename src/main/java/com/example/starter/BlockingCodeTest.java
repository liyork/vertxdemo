package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * Description:
 * Created on 2021/4/11 2:34 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class BlockingCodeTest extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.executeBlocking(future -> {
      System.out.println(Thread.currentThread().getName() + " executeBlocking");// worker-thread
      String result = blockingMethod1("hello");
      future.complete(result);
    }, res -> {
      System.out.println(Thread.currentThread().getName() + " The result1 is: " + res.result());// eventloop-thread
    });

    // 可以为不同的用途创建不同的池：
    //当使用同一个名字创建了许多 worker 时，它们将共享同一个 pool。
    // 当所有的 worker executor 调用了 close 方法被关闭过后，对应的 worker pool 会被销毁
    WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool");
    executor.executeBlocking(future -> {
      String result = blockingMethod2("hello");
      future.complete(result);
    }, res -> {
      System.out.println(Thread.currentThread().getName() + " The result2 is: " + res.result());// eventloop-thread
    });
    // Worker Executor 在不需要的时候必须被关闭
    //executor.close();
  }

  private String blockingMethod1(String hello) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "1";
  }

  private String blockingMethod2(String hello) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "2";
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(BlockingCodeTest.class.getName());

    // Worker Verticle
    DeploymentOptions options = new DeploymentOptions().setWorker(true);
    vertx.deployVerticle(new BlockingCodeTest2(), options);
  }

  static class BlockingCodeTest2 extends AbstractVerticle {

    @Override
    public void start() throws Exception {
      System.out.println(Thread.currentThread().getName() + " in BlockingCodeTest2");// worker-thread
    }
  }
}
