package com.wolf.inaction.beyondeventbus;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * SensorDataServiceVertxEBProxy可以通过mvn compile生成
 * 生成在vertxdemo/src/main/generated/com/wolf/inaction/beyondeventbus/SensorDataServiceVertxEBProxy.java
 * Created on 2021/5/28 7:11 AM
 *
 * @author 李超
 * @version 0.0.1
 */
@ProxyGen// is used to generate an event-bus proxy
public interface SensorDataService {
    static SensorDataService create(Vertx vertx) {
        return new SensorDataServiceImpl(vertx);
    }

    static SensorDataService createProxy(Vertx vertx, String address) {
        return new SensorDataServiceVertxEBProxy(vertx, address);
    }

    // 有参操作，约定为最后一个参数是callback
    void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler);

    void average(Handler<AsyncResult<JsonObject>> handler);// 无参操作
}
