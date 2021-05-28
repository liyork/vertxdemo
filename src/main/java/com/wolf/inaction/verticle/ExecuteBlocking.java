package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 执行阻塞动作，在worker池中，结果在eventloop中执行
 * executeBlocking中会构造出promise、构造task放入worker的队列，然后返回promise，
 * 当worker执行时，就是在那里线程执行blockingCodeHandler然后promise.complete时就触发maineventloop的执行
 * Created on 2021/5/24 10:22 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ExecuteBlocking extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteBlocking.class);

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(5000, id -> {
            logger.info("Tick");
            // true(默认)能保证结果顺序以相同的调用顺序返回,false则有结果就可以返回
            vertx.executeBlocking(this::blockingCode, true, this::resultHandler);
        });
    }

    private void blockingCode(Promise<String> promise) {
        logger.info("Blocking code running");
        try {
            Thread.sleep(4000);
            logger.info("Done!");
            promise.complete("Ok result");// 失败或成功，标志blocking代码执行完成
        } catch (InterruptedException e) {
            promise.fail(e);
        }
    }

    private void resultHandler(AsyncResult<String> ar) {// 结果处理还是在eventloop中
        if (ar.succeeded()) {
            logger.info("Blocking code result: {}", ar.result());
        } else {
            logger.error("Woops", ar.cause());
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ExecuteBlocking());
    }
}
