package com.wolf.inaction.verticle;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:verticle外用EventLoopContext
 * Created on 2021/5/25 9:18 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CreateContextWithoutVerticle {
    private static final Logger logger = LoggerFactory.getLogger(CreateContextWithoutVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // 这里很明显用的一个eventloop，怎么作者说两个?
        vertx.getOrCreateContext()
                .runOnContext(v -> logger.info("ABC"));// lambda is executed on a vertx context thread

        vertx.getOrCreateContext()
                .runOnContext(v -> logger.info("123"));

        Context ctx = vertx.getOrCreateContext();
        ctx.put("foo", "bar");

        ctx.exceptionHandler(t -> {
            if ("Tada".equals(t.getMessage())) {
                logger.info("Got a _Tada_ exception");
            } else {
                logger.error("Woops", t);
            }
        });

        ctx.runOnContext(v -> {
            throw new RuntimeException("Tada");
        });

        ctx.runOnContext(v -> {
            logger.info("foo = {}", (String) ctx.get("foo"));
        });
    }
}
