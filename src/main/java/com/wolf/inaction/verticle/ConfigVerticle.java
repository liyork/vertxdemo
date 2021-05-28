package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:使用配置
 * Created on 2021/5/24 9:54 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ConfigVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ConfigVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("n = {}", config().getInteger("n", -1));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        for (int n = 0; n < 4; n++) {
            JsonObject conf = new JsonObject().put("n", n);
            DeploymentOptions opts = new DeploymentOptions()
                    .setConfig(conf)// 传递configuration
                    .setInstances(n);// 部署多实例
            // 部署多实例，需要用fully qualified class name(FQCN)
            vertx.deployVerticle("com.wolf.inaction.SampleVerticle", opts);
        }
    }
}
