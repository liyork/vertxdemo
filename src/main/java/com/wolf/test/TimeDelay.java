package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * Description:在标准verticle里，你不能以使线程休眠的方式引入延迟；这样干会阻塞event loop线程.
 * 取而代之的是Vert.x定时器，定时器分为一次性（one-shot）和周期性（periodic）
 * Created on 2021/4/9 10:56 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class TimeDelay extends AbstractVerticle {
  // 如果是在verticle内部创建的定时器，那么verticle被卸载时，这些定时器将被自动关闭。
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // 一次性定时器
    vertx.setTimer(1000, id -> {
      System.out.println("And one second later this is printed");
    });

    // 周期性
    // (注意：定时器会定期触发。如果定期处理需要耗费大量时间，你的定时器事件可能会连续运行甚至糟糕到堆积在一起。
    // 在这种情况下，应该考虑转而使用setTimer。一旦你的处理完成了，你可以再设置下一个定时器。）
    vertx.setPeriodic(1000, id -> {
      System.out.println("And every second this is printed");
    });
  }
}

// EventLoopChecker
//Vert.x3内置了EventLoopChecker这个动态监测所有EventLoop线程的工具，默认EventLoop被阻塞了2秒钟的时候会触发报警，
// 如果持续阻塞则会直接打印那一块的异常栈到日志里，非常方便开发者来检查自己的异步代码。
