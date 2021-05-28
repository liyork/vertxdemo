package com.wolf.inaction.beyondcallback;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: 请求3个HeatSensor2服务然后发送给SnapshotService后,返回数据给前端
 * Created on 2021/5/27 9:06 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CollectorService extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(CollectorService.class);

    private WebClient webClient;

    @Override
    public void start() throws Exception {
        webClient = WebClient.create(vertx);
        vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .listen(8080);
    }

    private void handleRequest(HttpServerRequest request) {
        ArrayList<JsonObject> responses = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);// need an object to increment an integer from the callback
        for (int i = 0; i < 3; i++) {
            webClient.get(3000 + i, "localhost", "/")// issues an http get request /
                    .expect(ResponsePredicate.SC_SUCCESS)// trigger an error when http status code is not 2xx
                    .as(BodyCodec.jsonObject())// treat the body as a json object and perform automatic conversion
                    .send(ar -> {// in parallel
                        if (ar.succeeded()) {
                            responses.add(ar.result().body());
                        } else {
                            logger.error("Sensor down?", ar.cause());
                        }
                        if (counter.incrementAndGet() == 3) {
                            JsonObject data = new JsonObject()
                                    .put("data", new JsonArray(responses));
                            sendToSnapshot(request, data);
                        }
                    });
        }
    }

    private void sendToSnapshot(HttpServerRequest request, JsonObject data) {
        webClient
                .post(4000, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJsonObject(data, ar -> {
                    if (ar.succeeded()) {
                        sendResponse(request, data);
                    } else {
                        logger.error("Sendshot down?", ar.cause());
                        request.response().setStatusCode(500).end();
                    }
                });
    }

    private void sendResponse(HttpServerRequest request, JsonObject data) {
        request.response()
                .putHeader("Content-Type", "application/json")
                .end(data.encode());
    }
}
