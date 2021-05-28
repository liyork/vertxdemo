package com.wolf.inaction.beyondcallback;


import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.AbstractVerticle;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * curl http://localhost:8080
 * Created on 2021/5/27 1:38 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VertxRxJava extends AbstractVerticle {
    @Override
    public Completable rxStart() {// notifies of deployment success using a completable
        Observable
                .interval(1, TimeUnit.SECONDS, RxHelper.scheduler(vertx.getDelegate()))// scheduler enforces the vertx threading model
                .subscribe(n -> System.out.println("tick"));

        return vertx.createHttpServer()
                .requestHandler(r -> r.response().end("Ok"))
                .rxListen(8080)
                .ignoreElement();// return a completable from a single
    }

    public static void main(String[] args) {
        io.vertx.reactivex.core.Vertx vertx = new io.vertx.reactivex.core.Vertx(Vertx.vertx());
        vertx.deployVerticle(VertxRxJava.class.getName());
    }
}
