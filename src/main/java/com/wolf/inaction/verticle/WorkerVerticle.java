package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: wokerVerticle可能在不同的worker线程上执行
 * doDeploy中会有判断决定使用vertx.createWorkerContext还是vertx.createEventLoopContext
 * Created on 2021/5/24 10:12 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class WorkerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(WorkerVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(10_000, id -> {
            try {
                logger.info("Zzz...");
                Thread.sleep(8000);
                logger.info("Up!");
            } catch (InterruptedException e) {
                logger.error("Woops", e);
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions opts = new DeploymentOptions()
                .setInstances(2)
                .setWorker(true);// 设定verticle是workerVerticle
        vertx.deployVerticle(WorkerVerticle.class.getName(), opts);
    }
}
