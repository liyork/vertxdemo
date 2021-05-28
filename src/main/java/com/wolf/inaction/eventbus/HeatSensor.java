package com.wolf.inaction.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

/**
 * Description: 创造SENSOR_UPDATE消息并群发
 * 并不知道发送的值用做什么，所以用publish
 * Created on 2021/5/25 1:01 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class HeatSensor extends AbstractVerticle {
    private final Random random = new Random();
    protected final String sensorId = UUID.randomUUID().toString();// sensor identifier
    protected double temperature = 21.0;

    @Override
    public void start() throws Exception {
        scheduleNextUpdate();
    }

    private void scheduleNextUpdate() {
        vertx.setTimer(random.nextInt(5000) + 1000, this::update);// 延时触发
    }

    private void update(long timerId) {
        temperature = temperature + (delta() / 10);
        JsonObject payload = new JsonObject()
                .put("id", sensorId)
                .put("temp", temperature);
        vertx.eventBus().publish(SensorData.SENSOR_UPDATE, payload);// 群发
        scheduleNextUpdate();// 下次定时任务
    }

    private double delta() {
        if (random.nextInt() > 0) {
            return random.nextGaussian();
        } else {
            return -random.nextGaussian();
        }
    }
}
