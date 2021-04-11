package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Description:
 * Created on 2021/4/9 10:17 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class RestfulVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route()// 路由器
      .handler(BodyHandler.create());// 增加一个处理器，将请求的上下文信息，都放到RoutingContext中

    router.get("/get/:param1/:param2").handler(this::handleGet);
    router.route("/assets/*").handler(StaticHandler.create("assets"));
    vertx.createHttpServer().requestHandler(router).listen(8080);
  }

  private void handleGet(RoutingContext context) {
    String param1 = context.request().getParam("param1");
    String param2 = context.request().getParam("param2");

    if (isBlank(param1) || isBlank(param2)) {
      context.response().setStatusCode(400).end();
      //return;
    }
    JsonObject obj = new JsonObject();
    obj.put("method", "get").put("param1", param1).put("param2", param2);

    // 若不能一次输出所有response需要设置
    context.response().setChunked(true);

    context.response().putHeader("content-type", "application/json")
      .end(obj.encodePrettily());

    FileSystem fs = vertx.fileSystem();
    fs.copy("xx", "yy", res -> {// 最后run时调用了context.executeBlockingInternal
      if (res.succeeded()) {
        System.out.println("succ");
      } else {
        System.out.println("err");
      }
    });
  }

  private boolean isBlank(String str) {
    return str == null || "".equals(str);
  }

  // curl http://localhost:8080/get/1/2
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new RestfulVerticle());
  }
}
