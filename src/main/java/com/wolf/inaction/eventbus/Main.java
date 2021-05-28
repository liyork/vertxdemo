package com.wolf.inaction.eventbus;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * Description:
 * Created on 2021/5/25 1:57 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(HeatSensor.class.getName());
        vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(Listener.class.getName());
        vertx.deployVerticle(SensorData.class.getName());
        vertx.deployVerticle(HttpServer.class.getName());
    }
}
