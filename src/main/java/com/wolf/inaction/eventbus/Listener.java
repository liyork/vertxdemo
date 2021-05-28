package com.wolf.inaction.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

/**
 * Description: 消息接收并格式化打印
 * Created on 2021/5/25 1:05 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Listener extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final DecimalFormat format = new DecimalFormat("#.##");

    @Override
    public void start() throws Exception {
        EventBus bus = vertx.eventBus();
        bus.<JsonObject>consumer(SensorData.SENSOR_UPDATE, msg -> {// 订阅消息，提供callback
            JsonObject body = msg.body();// payload
            String id = body.getString("id");
            String temperature = format.format(body.getDouble("temp"));
            logger.info("{} reports a temperature ~{}C", id, temperature);
        });
    }
}
