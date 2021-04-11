package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Description:
 * Created on 2021/4/11 2:38 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ContextTest extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    Context context = vertx.getOrCreateContext();
    if (context.isEventLoopContext()) {
      System.out.println("Context attached to Event Loop");
    } else if (context.isWorkerContext()) {
      System.out.println("Context attached to Worker Thread");
    } else if (!Context.isOnVertxThread()) {
      System.out.println("Context not attached to a thread managed by vert.x");
    }

    System.out.println(Thread.currentThread().getName() + " in this verticle");

    // 可以在 Context 中异步执行代码了。提交的任务将会在同一个 Context 中运行：
    context.runOnContext(v -> {
      System.out.println(Thread.currentThread().getName() + " in this same context1");
      System.out.println("This will be executed asynchronously in the same context");
    });

    // 当在同一个 Context 中运行了多个处理函数时，可能需要在它们之间共享数据。 Context 对象提供了存储和读取共享数据的方法
    context.put("data", "hello");
    context.runOnContext((v) -> {
      String data = context.get("data");
      System.out.println(Thread.currentThread().getName() + " in this same context2, data:" + data);
    });
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(ContextTest.class.getName());
  }
}
