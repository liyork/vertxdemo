package com.wolf.inaction.eventbus.cluster;

import com.wolf.inaction.eventbus.HeatSensor;
import com.wolf.inaction.eventbus.HttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * Created on 2021/5/25 6:43 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class FirstInstance {
    private static final Logger logger = LoggerFactory.getLogger(FirstInstance.class);

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), ar -> {// 开启集群vertx
            if (ar.succeeded()) {
                logger.info("First instance has been started");
                Vertx vertx = ar.result();
                vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
                vertx.deployVerticle(HttpServer.class.getName());
            } else {
                logger.error("Could not start", ar.cause());
            }
        });
    }
}
