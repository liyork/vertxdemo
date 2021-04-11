package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

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

    testCompose();
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
