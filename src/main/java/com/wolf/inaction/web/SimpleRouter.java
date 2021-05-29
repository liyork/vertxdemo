package com.wolf.inaction.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * Description:
 * Created on 2021/5/29 7:38 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class SimpleRouter extends AbstractVerticle {
    @Override
    public void start() throws Exception {

        // 创建HttpServer
        HttpServer server = vertx.createHttpServer();

        // 创建路由对象
        Router router = Router.router(vertx);

        // 监听/index地址
        router.route("/index").handler(request -> {
            request.response().end("INDEX SUCCESS");
        });

        // 把请求交给路由处理--------------------(1)
        server.requestHandler(router);
        server.listen(8888);
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new SimpleRouter());
    }

}
