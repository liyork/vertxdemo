package com.wolf.inaction.asyndata_eventstream.jukebox;

import io.vertx.core.Vertx;

/**
 * Description: 文件流和输出流交互
 * 1.访问，http://localhost:8080
 * 2.控制，netcat localhost 3000
 * /list
 * /schedule a.mp3
 * /pause
 * /play
 * /schedule b.mp3
 * ^C
 * 3.下载，curl -o out.mp3 http://localhost:8080/download/a.mp3
 * Created on 2021/5/26 12:52 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Jukebox.class.getName());
        vertx.deployVerticle(NetControl.class.getName());
    }
}
