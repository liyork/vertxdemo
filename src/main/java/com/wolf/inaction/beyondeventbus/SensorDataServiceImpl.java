package com.wolf.inaction.beyondeventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Description:
 * Created on 2021/5/28 7:12 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SensorDataServiceImpl implements SensorDataService, SensorDataServiceWithRx {
    private final HashMap<String, Double> lastValues = new HashMap<>();

    SensorDataServiceImpl(Vertx vertx) {
        vertx.eventBus().<JsonObject>consumer("sensor.updates", message -> {
            JsonObject json = message.body();
            lastValues.put(json.getString("id"), json.getDouble("temp"));
        });
    }

    @Override
    public void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler) {
        if (lastValues.containsKey(sensorId)) {
            JsonObject data = new JsonObject()
                    .put("sensorId", sensorId)
                    .put("value", lastValues.get(sensorId));
            handler.handle(Future.succeededFuture(data));// 不用reply，而是用异步result
        } else {
            handler.handle(Future.failedFuture("No value has been observed for " + sensorId));
        }
    }

    @Override
    public void average(Handler<AsyncResult<JsonObject>> handler) {
        Double avg = lastValues.values().stream()
                .collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject data = new JsonObject().put("average", avg);
        handler.handle(Future.succeededFuture(data));
    }
}
