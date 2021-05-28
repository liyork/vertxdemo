package com.wolf.inaction.beyondeventbus;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen// allow code generation
public interface SensorDataServiceWithRx {
    static SensorDataServiceWithRx create(Vertx vertx) {
        return new SensorDataServiceImpl(vertx);
    }

    static SensorDataServiceWithRx createProxy(Vertx vertx, String address) {
        return new SensorDataServiceWithRxVertxEBProxy(vertx, address);
    }

    // 有参操作，约定为最后一个参数是callback
    void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler);

    void average(Handler<AsyncResult<JsonObject>> handler);// 无参操作
}
