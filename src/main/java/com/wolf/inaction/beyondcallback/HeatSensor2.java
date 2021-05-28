package com.wolf.inaction.beyondcallback;

import com.wolf.inaction.eventbus.HeatSensor;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * Created on 2021/5/27 7:26 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class HeatSensor2 extends HeatSensor {
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .listen(config().getInteger("http.port", 3000));
        super.start();
    }

    private void handleRequest(HttpServerRequest req) {
        JsonObject data = new JsonObject()
                .put("id", sensorId)
                .put("temp", temperature);
        req.response()
                .putHeader("Content-Type", "application/json")
                .end(data.encode());
    }
}
