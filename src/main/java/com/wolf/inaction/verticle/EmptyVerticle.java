package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * Created on 2021/5/24 6:38 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class EmptyVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(EmptyVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("Start");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stop");
    }
}
