package com.wolf.inaction.beyondeventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * Description:
 * Created on 2021/5/28 7:19 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class DataVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        new ServiceBinder(vertx)// binds a service to an address
                .setAddress("sensor.data-service")// the event-bus address for the service
                .register(SensorDataService.class, SensorDataService.create(vertx));
    }
}
