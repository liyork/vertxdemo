package com.wolf.inaction.beyondcallback;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Description: 使用future重写
 * Created on 2021/5/27 9:06 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CollectorServiceWithFuture extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(CollectorServiceWithFuture.class);

    private WebClient webClient;

    @Override
    public void start() throws Exception {
        webClient = WebClient.create(vertx);
        vertx.createHttpServer()
                .requestHandler(this::handleRequestWithFuture)
                .listen(8080);
    }

    private void handleRequestWithFuture(HttpServerRequest request) {
        CompositeFuture.all(// compose several futures
                fetchTemperature(3000),
                fetchTemperature(3001),
                fetchTemperature(3002))// complete when all future are completed, fails when any future has failed
                .flatMap(this::sendToSnapshotWithFuture)// chain with another asynchronous operation
                .onSuccess(data -> request.response()
                        .putHeader("Content-Type", "application/json")
                        .end(data.encode()))
                .onFailure(err -> {
                    logger.error("Something went wrong", err);
                    request.response().setStatusCode(500).end();
                });
    }

    private Future<JsonObject> sendToSnapshotWithFuture(CompositeFuture temps) {
        List<JsonObject> tempData = temps.list();
        JsonObject data = new JsonObject()
                .put("data", new JsonArray()
                        .add(tempData.get(0))
                        .add(tempData.get(1))
                        .add(tempData.get(2)));
        return webClient
                .post(4000, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJson(data)// future-based variant
                .map(response -> data);
    }

    private Future<JsonObject> fetchTemperature(int port) {
        return webClient
                .get(port, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .send()// a future
                .map(HttpResponse::body);
    }

}
