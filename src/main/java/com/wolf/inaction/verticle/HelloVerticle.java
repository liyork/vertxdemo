package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * curl http://localhost:8080/
 * <p>
 * event processing happens on a single event-loop thread
 * an obvious benefit of this desing is that a verticle instance always executes event processing
 * on the same thread.
 * Created on 2021/5/24 1:33 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class HelloVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(HelloVerticle.class);
    private long counter = 1;

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(5000, id -> {// defines a periodic task
            logger.info("tick");
        });

        vertx.createHttpServer()
                .requestHandler(req -> {// calls on every reqeust
                    logger.info("Request #{} from {}", counter++, req.remoteAddress().host());
                    req.response().end("Hello!");
                })
                .listen(8080);
        logger.info("Open http://localhost:8080/");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();// a global vert.x instance
        vertx.deployVerticle(new HelloVerticle());// deploy a verticle
    }
}
