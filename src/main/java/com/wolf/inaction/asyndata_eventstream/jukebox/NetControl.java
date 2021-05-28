package com.wolf.inaction.asyndata_eventstream.jukebox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 控制，用eventbus发事件
 * netcat localhost:3000 xxxx
 * /list
 * /play
 * /pause
 * /schedule file
 * Created on 2021/5/26 9:27 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class NetControl extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(NetControl.class);

    @Override
    public void start() throws Exception {
        vertx.createNetServer()
                .connectHandler(this::handleClient)
                .listen(3000);
    }

    private void handleClient(NetSocket socket) {
        RecordParser.newDelimited("\n", socket)// parse by looking for new lines
                .handler(buffer -> handleBuffer(socket, buffer))// now buffers are lines
                .endHandler(v -> logger.info("Connection ended"));
    }

    private void handleBuffer(NetSocket socket, Buffer buffer) {
        String command = buffer.toString();// decode with default charset
        switch (command) {
            case "/list":
                listCommand(socket);
                break;
            case "/play":
                vertx.eventBus().send("jukebox.play", "");
                break;
            case "/pause":
                vertx.eventBus().send("jukebox.pause", "");
                break;
            default:
                if (command.startsWith("/schedule ")) {
                    schedule(command);
                } else {
                    socket.write("Unknown command\n");
                }
        }
    }

    private void schedule(String command) {
        String track = command.substring(10);// 前10个是/schedule和空格
        JsonObject json = new JsonObject().put("file", track);
        vertx.eventBus().send("jukebox.schedule", json);
    }

    private void listCommand(NetSocket socket) {
        vertx.eventBus().request("jukebox.list", "", reply -> {
            if (reply.succeeded()) {
                JsonObject data = (JsonObject) reply.result().body();
                data.getJsonArray("files")
                        .stream().forEach(name -> socket.write(name + "\n"));
            } else {
                logger.error("/list error", reply.cause());
            }
        });
    }
}
