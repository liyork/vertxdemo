package com.wolf.inaction.simpleeventloop;

/**
 * Description:
 * Created on 2021/5/24 9:09 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SimpleEventLoopTest {
  public static void main(String[] args) {
    EventLoop eventLoop = new EventLoop();

    new Thread(() -> {// 第一线程每秒放入事件
      for (int n = 0; n < 6; n++) {
        delay(1000);
        eventLoop.dispatch(new EventLoop.Event("tick", n));
      }
      eventLoop.dispatch(new EventLoop.Event("stop", null));
    }).start();

    new Thread(() -> {// 第二个线程放入两个事件在2500ms和3300ms
      delay(2500);
      eventLoop.dispatch(new EventLoop.Event("hello", "beautiful world"));
      delay(800);
      eventLoop.dispatch(new EventLoop.Event("hello", "beautiful universe"));
    }).start();

    eventLoop.dispatch(new EventLoop.Event("hello", "world"));// 主线程放入事件
    eventLoop.dispatch(new EventLoop.Event("foo", "bar"));

    eventLoop.on("hello", s -> System.out.println("hello " + s))// 事件处理
      .on("tick", n -> System.out.println("tick #" + n))
      .on("stop", v -> eventLoop.stop())
      .run();// 本线程执行

    System.out.println("Bye!");
  }

  private static void delay(long millis) {// 包装受检查异常到非检查异常，避免污染主方法的代码处理逻辑
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

