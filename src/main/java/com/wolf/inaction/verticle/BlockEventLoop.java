package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * Description:阻塞动作在eventloop上执行则会有warn告警
 * Created on 2021/5/24 1:52 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class BlockEventLoop extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.setTimer(1000, id -> {
            while (true) ;// infinite loop
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new BlockEventLoop());
    }
}
