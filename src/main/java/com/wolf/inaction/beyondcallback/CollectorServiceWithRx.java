package com.wolf.inaction.beyondcallback;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 使用rxJava重写
 * Created on 2021/5/27 9:06 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CollectorServiceWithRx extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(CollectorServiceWithRx.class);

    private WebClient webClient;

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        return vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .rxListen(8080)// a Single
                .ignoreElement();// a completable
    }

    private Single<JsonObject> collectTemperatures() {
        Single<HttpResponse<JsonObject>> r1 = fetchTemperature(3000);
        Single<HttpResponse<JsonObject>> r2 = fetchTemperature(3001);
        Single<HttpResponse<JsonObject>> r3 = fetchTemperature(3002);

        // when all http responses are available, zip operator passes value to function
        return Single.zip(r1, r2, r3, (j1, j2, j3) -> {// compose three response
            JsonArray array = new JsonArray()
                    .add(j1.body())
                    .add(j2.body())
                    .add(j3.body());
            return new JsonObject().put("data", array);
        });
    }

    private void handleRequest(HttpServerRequest request) {
        Single<JsonObject> data = collectTemperatures();
        sendToSnapshot(data).subscribe(json -> {// send data to snapshot service
            request.response()
                    .putHeader("Content-Type", "application/json")
                    .end(json.encode());
        }, err -> {// have a single point of error management
            logger.error("Something went wrong", err);
            request.response().setStatusCode(500).end();
        });
    }

    private Single<JsonObject> sendToSnapshot(Single<JsonObject> data) {
        return data.flatMap(json -> webClient
                .post(4000, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .rxSendJson(json)// send a json object, then reports on the http request response
                .flatMap(resp -> Single.just(json))// give back the json object
        );
    }

    private Single<HttpResponse<JsonObject>> fetchTemperature(int port) {
        return webClient
                .get(port, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .rxSend();// return a Single
    }

}
