package com.wolf.inaction.beyondcallback;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Description:
 * Created on 2021/5/27 12:28 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VertxFuture {
    public static void main(String[] args) {
        Promise<Object> promise = Promise.promise();

        Vertx vertx = Vertx.vertx();
        vertx.createHttpServer()
                .requestHandler(System.out::println)
                .listen(8080)// return a future。比.listen(8080, ar -> {这种方式优雅
                .onFailure(promise::fail)// call when fail
                .onSuccess(ok -> {// call when success
                    System.out.println("http://localhost:8080/");
                    promise.complete();
                });
    }
}
