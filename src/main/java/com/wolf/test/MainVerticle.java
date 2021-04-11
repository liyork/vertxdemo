package com.wolf.test;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

// start和stop
//这两个方法在Verticle实例被注册的Vertx实例或者从Vertx实例卸载的时候被自动调用。
public class MainVerticle extends AbstractVerticle {

  // 同步
  @Override
  public void start() throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888);
  }

  // 异步
  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    System.out.println(config());// 获取配置

    // 环境变量
    System.getProperty("prop");
    System.getenv("HOME");

    vertx.createHttpServer().requestHandler(req -> {
      HttpServerResponse response = req.response();
      response
        .putHeader("content-type", "text/plain")
        .write("Hello from Vert.x!");
      response.end();
    }).listen(8888, http -> {
      // 标记启动完成或失败了
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  public void stop(Promise<Void> stopPromise) {
    //obj.doSomethingThatTakesTime(res -> {
    //    if (res.succeeded()) {
    //        stopPromise.complete();
    //    } else {
    //        stopPromise.fail();
    //    }
    //});
  }

  public static void main(String[] args) {
    // Vertex对象是Vert.x的控制中心，创建客户端和服务器、获取事件总线（event bus）的引用、设置定时器等功能都需要该实例
    //该Vertx实例，其实并不能做具体事情。为了让程序能做一些有用的事情，需要将自定义的MyVerticle实例注册到Vertx
    //创建一个vertx实例
    VertxOptions vo = new VertxOptions();
    vo.setEventLoopPoolSize(1);
    Vertx vertx = Vertx.vertx(vo);

    //vertx.setPeriodic(1000, id -> {
    //  // This handler will get called every second
    //  System.out.println("timer fired!");
    //});

    // deploy
    Verticle myVerticle = new MainVerticle();
    //vertx.deployVerticle(myVerticle);

    // 部署verticle的实例数
    //new DeploymentOptions().setInstances(2);

    //设定配置
    JsonObject config = new JsonObject().put("name", "tim").put("directory", "/blah");
    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    // 设定工作池名称
    //vertx.deployVerticle("the-verticle", new DeploymentOptions().setWorkerPoolName("the-specific-pool"));

    // 部署是异步的，可以知道部署成功与否
    vertx.deployVerticle("com.example.starter.MainVerticle", res -> {
      if (res.succeeded()) {
        System.out.println("Deployment id is: " + res.result());//This deployment ID can be used later if you want to undeploy the deployment.
      } else {
        System.out.println("Deployment failed!");
      }
    });// 和上面一样

    // undeploy
    // 撤销操作也是异步的
    //String deploymentID = null;
    //vertx.undeploy(deploymentID, res -> {
    //  if (res.succeeded()) {
    //    System.out.println("Undeployed ok");
    //  } else {
    //    System.out.println("Undeploy failed!");
    //  }
    //});
  }

  // 阻塞操作操作代码如下：
  public void workerExecutor1(Vertx vertx) {
    WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool");
    executor.executeBlocking(promise -> {
      // Call some blocking API that takes a significant amount of time to return
      //String result = someAPI.blockingMethod("hello");
      String result = null;
      promise.complete(result);
    }, res -> {
      System.out.println("The result is: " + res.result());
    });
  }
}
