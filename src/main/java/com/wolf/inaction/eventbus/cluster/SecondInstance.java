package com.wolf.inaction.eventbus.cluster;

import com.wolf.inaction.eventbus.HttpServer;
import com.wolf.inaction.eventbus.Listener;
import com.wolf.inaction.eventbus.SensorData;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: curl http://localhost:8081/sse
 * two instances will discover each other
 * Created on 2021/5/25 6:44 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SecondInstance {
    private static final Logger logger = LoggerFactory.getLogger(SecondInstance.class);

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), ar -> {
            if (ar.succeeded()) {
                logger.info("Second instance has been started");
                Vertx vertx = ar.result();
                //vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
                vertx.deployVerticle(Listener.class.getName());
                vertx.deployVerticle(SensorData.class.getName());
                JsonObject conf = new JsonObject().put("port", 8081);
                vertx.deployVerticle(HttpServer.class.getName(), new DeploymentOptions().setConfig(conf));
                //vertx.eventBus().localConsumer()// 消费本vertx的事件
            } else {
                logger.error("Could not start", ar.cause());
            }
        });
    }
}
