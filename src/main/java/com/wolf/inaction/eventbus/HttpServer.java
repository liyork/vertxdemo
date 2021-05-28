package com.wolf.inaction.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.TimeoutStream;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 * 接收http请求，订阅SENSOR_UPDATE，并定时发送SENSOR_AVG
 * curl http://localhost:8080/sse --stream
 * Created on 2021/5/25 1:10 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class HttpServer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(this::handler)
                .listen(config().getInteger("port", 8080));// 配置端口，默认8080
    }

    private void handler(HttpServerRequest request) {
        if ("/".equals(request.path())) {
            request.response().sendFile("index.html");// 本地文件已流方式发送给client，自动关闭连接
        } else if ("/sse".equals(request.path())) {
            sse(request);// server-sent event
        } else {
            request.response().setStatusCode(404);
        }
    }

    // server-sent events
    private void sse(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response
                .putHeader("Content-Type", "text/event-stream")// for server-sent events
                .putHeader("Cache-Control", "no-cache")// 不缓存
                .setChunked(true);

        // 订阅变更
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(SensorData.SENSOR_UPDATE);
        consumer.handler(msg -> {
            response.write("event: update\n");
            response.write("data: " + msg.body().encode() + "\n\n");
        });

        // 单发SENSOR_AVG并进行回应,写回response
        TimeoutStream ticks = vertx.periodicStream(1000);// 定时流
        ticks.handler(id -> {
            // request sends a message that expects a response
            vertx.eventBus().<JsonObject>request(SensorData.SENSOR_AVG, "",
                    reply -> {// reply是个异步对象，可能失败
                        if (reply.succeeded()) {
                            response.write("event:average\n");
                            response.write("data: " + reply.result().body().encode() + "\n\n");
                        }
                    });
        });

        // 当客户端disconnect/或refresh页面时触发，反注册订阅和取消定时任务
        response.endHandler(v -> {
            logger.info("disconnect...");
            consumer.unregister();
            ticks.cancel();
        });
    }
}
