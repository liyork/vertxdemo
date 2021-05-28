package com.wolf.inaction.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * Description:异步Verticle完成通知
 * Created on 2021/5/24 6:28 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class AsyncCompleteVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> promise) throws Exception {// 泛型是void，vertx is only interested in the deployment completion
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Ok"))
                .listen(8080, ar -> {
                    if (ar.succeeded()) {// 异步结果指示是否成功
                        promise.complete();// 标记promise完成
                    } else {
                        promise.fail(ar.cause());// 标记promise失败，并传递错误
                    }
                });
    }
}
