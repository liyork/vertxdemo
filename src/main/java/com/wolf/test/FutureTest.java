package com.wolf.test;

import io.vertx.core.*;

/**
 * Description:
 * Created on 2021/4/11 2:28 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class FutureTest extends AbstractVerticle {

  public void start() throws Exception {
    Future<Integer> future = vertx.executeBlocking(event -> {
      System.out.println("1 hello");
    });

    //future.compose((Function<Integer, String>) o -> {
    //  System.out.println("2 hello");
    //  return o.toString();
    //});
  }

  public static void main(String[] args) {
    //Future<String> future = testFuture();

    //testCompose();

    //testComplete();

    testCompositeFutureAll();

    //testCompositeFutureAny();
  }

  // 都succ则返回succ
  private static void testCompositeFutureAll() {
    Future<String> f1 = Future.future(promise -> {
      System.out.println("1111");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promise.complete();
      //promise.fail("xxx");
    });

    Future<String> f2 = Future.future(promise -> {
      System.out.println("2222");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promise.complete();
      promise.fail("yyy");
    });

    CompositeFuture.all(f1, f2).onComplete(e -> {
      if (e.succeeded()) {
        System.out.println("CompositeFuture.all onComplete succ ," + e.result());
      } else {
        System.out.println("CompositeFuture.all onComplete fail " + e.result());
      }
    });
  }

  // 一个succ则succ
  private static void testCompositeFutureAny() {
    Future<String> f1 = Future.future(promise -> {
      System.out.println("1111");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promise.complete();
      //promise.fail("xxx");
    });

    Future<String> f2 = Future.future(promise -> {
      System.out.println("2222");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      promise.complete();
      promise.fail("yyy");
    });

    CompositeFuture.any(f1, f2).onComplete(e -> {
      if (e.succeeded()) {
        System.out.println("CompositeFuture.all onComplete succ ," + e.result());
      } else {
        System.out.println("CompositeFuture.all onComplete fail " + e.result());
      }
    });
  }

  private static void testComplete() {
    Future<String> future = Future.future(promise -> {
      System.out.println("1111");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //promise.complete();
      promise.fail("xxx");
    });

    future.onComplete((AsyncResult<String> event) -> {
      System.out.println("Complete. " + event.result());
    });
  }

  // 成功则继续否则退出
  private static void testCompose() {
    Future<String> future = Future.future(promise -> {
      System.out.println("1111");
      //promise.complete();
      promise.fail("xxxx");
    });

    Future<String> compose = future.compose(res -> {
      Future<String> result = Future.future(pro -> {
        System.out.println("22");
        pro.complete();
      });
      return result;
    });

    compose.onSuccess(res -> {
      System.out.println("success, res " + res);
    }).onFailure(res -> {
      System.out.println("fail, msg: " + res.getMessage() + ", cause: " + res.getCause());
    });
  }

  private static Future<String> testFuture() {
    Future<String> future = Future.future(promise -> {
      System.out.println("1111");
      promise.complete();
      //promise.fail("xxxx");
    });

    future.onSuccess(res -> {
      System.out.println("success, res " + res);
    });
    future.onFailure(res -> {
      System.out.println("fail, msg: " + res.getMessage() + ", cause: " + res.getCause());
    });
    return future;
  }

}
