package com.wolf.inaction.beyondcallback;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Description:
 * Created on 2021/5/27 12:35 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CompletionStageTest {
    public static void main(String[] args) {
        //testFuture2CompletionStage();
        testCompletionStage2Future();
    }

    private static void testFuture2CompletionStage() {
        Promise<Object> promise = Promise.promise();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.complete("111");
        }).start();

        CompletionStage<Object> cs = promise.future().toCompletionStage();
        cs
                .thenApply(a -> a.toString().toUpperCase())
                .thenApply(str -> "~~~ " + str)
                .whenComplete((str, err) -> {// takes a value or an error
                    if (err == null) {
                        System.out.println(str);
                    } else {
                        System.out.println("Ho..." + err.getMessage());
                    }
                });
    }

    private static void testCompletionStage2Future() {
        Vertx vertx = Vertx.vertx();

        // create a cf from an asynchronous operation
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "5 seconds have elapsed";
        });

        Future
                .fromCompletionStage(cf, vertx.getOrCreateContext())// convert to a vertx future and dispatch on a vertx context
                .onSuccess(System.out::println)
                .onFailure(Throwable::printStackTrace);
    }
}
