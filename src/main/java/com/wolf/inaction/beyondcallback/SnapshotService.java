package com.wolf.inaction.beyondcallback;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 收集并日志
 * Created on 2021/5/27 9:00 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SnapshotService extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotService.class);

    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if (badRequest(req)) {
                        req.response().setStatusCode(400).end();
                    }
                    // waits for the whole body to be received rather than assembling intermediate buffer
                    req.bodyHandler(buffer -> {
                        logger.info("Latest temperatures: {}", buffer.toJsonObject().encodePrettily());
                        req.response().end();
                    });
                }).listen(config().getInteger("http.port", 4000));
    }

    private boolean badRequest(HttpServerRequest req) {
        return !req.method().equals(HttpMethod.POST) ||
                !"application/json".equals(req.getHeader("Content-Type"));
    }
}
