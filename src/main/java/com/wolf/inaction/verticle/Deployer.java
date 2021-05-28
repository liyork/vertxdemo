package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:用Deployer部署其他verticle
 * typical way to deply an pplication composed of verticls is as follows:
 * deploy a main verticle
 * the main verticle deploys other verticles
 * the deployed verticles may in turn deploy futher verticles
 * <p>
 * main在一个eventloop
 * 部署的其他verticle也在不同的eventloop中
 * <p>
 * default, vert.x creates twice the number of event-loop threads as CPU cores.
 * the assignment of verticles to event loops is done in a roundrobin fashion
 * a verticle always uses the same eventloop thread, the eventloop threads are being shared by multiple verticle
 * <p>
 * 每次部署都会从eventloopgroup中roundrobin获取一个eventloo放入其task中
 * Created on 2021/5/24 6:39 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Deployer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Deployer.class);

    @Override
    public void start() throws Exception {
        long delay = 1000;
        for (int i = 0; i < 10; i++) {
            vertx.setTimer(delay, id -> deploy());
            delay = delay + 1000;
        }
    }

    private void deploy() {
        vertx.deployVerticle(new EmptyVerticle(), ar -> {// 异步部署
            if (ar.succeeded()) {
                String id = ar.result();
                logger.info("Successfully deployed {}", id);
                vertx.setTimer(5000, tid -> undeployLater(id));
            } else {
                logger.error("Error while deploying", ar.cause());
            }
        });
    }

    private void undeployLater(String id) {
        vertx.undeploy(id, ar -> {
            if (ar.succeeded()) {
                logger.info("{} was undeployed", id);
            } else {
                logger.error("{} could not be undeployed", id);
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Deployer());
    }
}
