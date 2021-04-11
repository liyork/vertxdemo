package com.wolf.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;

/**
 * Description:
 * Created on 2021/4/11 2:27 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ClientUseTest extends AbstractVerticle {
  //将客户端对象与Verticle对象绑定，这里选取了三种不同的客户端作为示范
  HttpClient httpClient;

  // 使用客户端完之后，无需调用client.close()方法关闭客户端，频繁创建销毁客户端会在一定程度上消耗系统资源，降低性能，同时增加开发人员的负担，
  // Vert.x提供客户端的目的就在于复用连接以减少资源消耗，提升性能，同时简化代码，减轻开发人员的负担。
  // 如您关闭客户端，在下一次使用该客户端的时候，还需要重新创建客户端。
  public void start() throws Exception {
    httpClient = vertx.createHttpClient();
  }
}
