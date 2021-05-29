package com.wolf.inaction.beyondeventbus;

import io.vertx.core.AbstractVerticle;

/**
 * Description:
 * Created on 2021/5/28 8:55 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Main extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        //useGenEventBus();
        //useGenRx();
    }

    private void useGenRx() {
        //Vertx vertx = new Vertx(this.vertx);
        //SensorDataServiceWithRx service =
        //        SensorDataServiceWithRx.createProxy(vertx, "sensor.data-service");
        //
        //service.rxAverage()// single
        //        .delaySubscription(3, TimeUnit.SECONDS, RxHelper.scheduler(this.vertx))
        //        .repeat()
        //        .map(data -> "avt = " + data.getDouble("average"))
        //        .subscribe(System.out::println);
    }

    private void useGenEventBus() {
        //SensorDataService service = SensorDataService.createProxy(vertx, "sensor.data-service");
        //service.average(ar -> {
        //    if (ar.succeeded()) {
        //        System.out.println("Average = " + ar.result());
        //    } else {
        //        ar.cause().printStackTrace();
        //    }
        //});
    }
}
