package com.wolf.inaction.beyondcallback;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * curl http://localhost:8080
 * Created on 2021/5/27 9:15 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class AdgeService {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // each instance use a different port number
        vertx.deployVerticle(HeatSensor2.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3000)));

        vertx.deployVerticle(HeatSensor2.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3001)));

        vertx.deployVerticle(HeatSensor2.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3002)));

        vertx.deployVerticle(SnapshotService.class.getName());
        //vertx.deployVerticle(CollectorService.class.getName());
        //vertx.deployVerticle(CollectorServiceWithFuture.class.getName());
        //vertx.deployVerticle(CollectorServiceWithRx.class.getName());
        vertx.deployVerticle(CollectorServiceWithCoroutine.class.getName());
    }
}
