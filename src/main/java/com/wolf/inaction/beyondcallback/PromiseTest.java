package com.wolf.inaction.beyondcallback;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.Locale;

/**
 * Description:
 * Created on 2021/5/27 12:11 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class PromiseTest {
    public static void main(String[] args) throws InterruptedException {
        Promise<Object> promise = Promise.promise();
        Future<Object> future = promise.future();// derive a future from a promise

        Vertx vertx = Vertx.vertx();
        vertx.setTimer(5000, id -> {// asynchronous operation
            if (System.currentTimeMillis() % 2L == 0L) {
                promise.complete("Ok");// complete the promise with a value
            } else {
                promise.fail(new RuntimeException("Bad luck..."));// fail the promise with a exception
            }
        });

        future
                .onSuccess(System.out::println)// callback for when the promise is completed
                .onFailure(err -> {// callback for when the future is failed
                    System.out.println("onFailure " + err.getMessage());
                });

        future
                .recover(err -> Future.succeededFuture("Let's say it's ok"))// recover from an error with another value
                .map(a -> a.toString().toUpperCase(Locale.ROOT))
                .flatMap(str -> {// compose with another asynchronous operation
                    Promise<Object> next = Promise.promise();
                    vertx.setTimer(3000, id -> next.complete(">>> " + str));
                    return next.future();
                })
                .onSuccess(System.out::println);// 对新future操作

        Thread.sleep(10 * 1000);
        vertx.close();
    }
}
