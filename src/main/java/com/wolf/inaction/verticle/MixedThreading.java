package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Description:vertx系线程和非vertx系线程交互
 * Created on 2021/5/25 9:33 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class MixedThreading extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MixedThreading.class);

    @Override
    public void start() throws Exception {
        // get the context of the verticle
        // because start is running on an eventloop thread
        Context context = vertx.getOrCreateContext();
        new Thread(() -> {
            try {
                run(context);
            } catch (InterruptedException e) {
                logger.error("Woops", e);
            }
        }).start();
    }

    private void run(Context context) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        logger.info("I am in a non-Vertx thread");
        context.runOnContext(v -> {// run some code on the verticle eventloop thread
            logger.info("I am on the event-loop");
            vertx.setTimer(1000, id -> {
                logger.info("This is the final countdown");
                latch.countDown();
            });
        });
        logger.info("Waiting on the countdown latch...");
        latch.await();
        logger.info("Bye!");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MixedThreading());
    }
}
