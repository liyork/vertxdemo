package com.wolf.inaction.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Description:接收SENSOR_UPDATE并存储，当有HttpServer进行SENSOR_AVG时进行回复
 * Created on 2021/5/25 1:07 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SensorData extends AbstractVerticle {
    private final HashMap<String, Double> lastValue = new HashMap<String, Double>();// 每个sensor的值
    public static final String SENSOR_UPDATE = "sensor.updates";
    public static final String SENSOR_AVG = "sensor.average";

    @Override
    public void start() throws Exception {
        EventBus bus = vertx.eventBus();
        bus.consumer(SENSOR_UPDATE, this::update);
        bus.consumer(SENSOR_AVG, this::average);
    }

    private void update(Message<JsonObject> message) {
        JsonObject json = message.body();
        lastValue.put(json.getString("id"), json.getDouble("temp"));
    }

    private void average(Message<JsonObject> message) {// 请求的消息
        double avg = lastValue.values().stream().collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject json = new JsonObject().put("average", avg);
        message.reply(json);// 回复消息
    }
}
