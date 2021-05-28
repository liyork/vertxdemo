package com.wolf.inaction.asyndata_eventstream.jukebox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 接收事件，可用浏览器访问
 * Created on 2021/5/26 9:21 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class Jukebox extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Jukebox.class);

    enum State {PLAYING, PAUSED}// 单线程访问verticle

    public State currentMode = State.PAUSED;
    private final Queue<String> playlist = new ArrayDeque<>();

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("jukebox.list", this::list);
        eventBus.consumer("jukebox.schedule", this::schedule);
        eventBus.consumer("jukebox.play", this::play);
        eventBus.consumer("jukebox.pause", this::pause);

        vertx.createHttpServer()
                .requestHandler(this::httpHandler)
                .listen(8080);

        // periodically pushes new MP3 data
        vertx.setPeriodic(100, this::streamAudioChunk);
    }

    private void play(Message<?> request) {
        currentMode = State.PLAYING;
    }

    private void pause(Message<?> request) {
        currentMode = State.PAUSED;
    }

    // 加入播放列表
    private void schedule(Message<JsonObject> request) {
        String file = request.body().getString("file");
        if (playlist.isEmpty() && currentMode == State.PAUSED) {
            currentMode = State.PLAYING;
        }
        playlist.offer(file);
    }

    private void list(Message<?> request) {
        vertx.fileSystem().readDir("tracks", ".*mp3$", ar -> {// 异步
            if (ar.succeeded()) {
                List<String> files = ar.result()
                        .stream()
                        .map(File::new)
                        .map(File::getName)
                        .collect(Collectors.toList());
                JsonObject json = new JsonObject().put("files", new JsonArray(files));
                request.reply(json);
            } else {
                logger.error("readDir failed", ar.cause());
                request.fail(500, ar.cause().getMessage());// 发送失败
            }
        });
    }

    private void httpHandler(HttpServerRequest request) {
        if ("/".equals(request.path())) {
            openAudioStream(request);
            return;
        }

        if (request.path().startsWith("/download/")) {
            // 避免读取从/etc/passwd等
            String sanitizedPath = request.path().substring(10).replace("/", "");
            download(sanitizedPath, request);
            return;
        }
        request.response().setStatusCode(404).end();
    }

    private final Set<HttpServerResponse> streamers = new HashSet<>();

    private void openAudioStream(HttpServerRequest request) {
        HttpServerResponse response = request.response()
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);// 是stream,不知道长度
        streamers.add(response);
        response.endHandler(v -> {
            streamers.remove(response);// 流退出
            logger.info("A streamer left");
        });
    }

    private void download(String path, HttpServerRequest request) {
        String file = "tracks/" + path;
        if (!vertx.fileSystem().existsBlocking(file)) {// 本地很快，用阻塞，避免嵌套callback
            request.response().setStatusCode(404).end();
            return;
        }
        OpenOptions opts = new OpenOptions().setRead(true);
        vertx.fileSystem().open(file, opts, ar -> {
            if (ar.succeeded()) {
                downloadFile(ar.result(), request);
            } else {
                logger.error("Read failed", ar.cause());
                request.response().setStatusCode(500).end();
            }
        });
    }

    private void downloadFile(AsyncFile file, HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(200)
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);

        // 由于常用，则放入公共代码了，被下面替换
        //file.handler(buffer -> {
        //    response.write(buffer);
        //    if (response.writeQueueFull()) {// 若写太快
        //        file.pause();// back-pressure 通过停止read stream
        //        response.drainHandler(v -> file.resume());// 当drained则恢复
        //    }
        //});
        //file.endHandler(v -> response.end());

        file.pipeTo(response);// pipes data from file to response
    }

    private AsyncFile currentFile;
    private long positionInFile;

    private void streamAudioChunk(long id) {
        if (currentMode == State.PAUSED) {
            return;
        }
        if (currentFile == null && playlist.isEmpty()) {
            currentMode = State.PLAYING;
            return;
        }
        if (currentFile == null) {
            openNextFile();
        }
        // Buffers cannot be reused across I/O operations, os create a one
        currentFile.read(Buffer.buffer(4096), 0, positionInFile, 4096, ar -> {
            if (ar.succeeded()) {
                processReadBuffer(ar.result());// copy data to all players
            } else {
                logger.error("Read failed", ar.cause());
                closeCurrentFile();
            }
        });
    }

    // vertx. buffers cannot be reused once they have been written, as they are placed
    // in a write queue.
    private void processReadBuffer(Buffer buffer) {
        positionInFile += buffer.length();
        if (buffer.length() == 0) {// when end of file has been reached
            closeCurrentFile();
            return;
        }
        for (HttpServerResponse streamer : streamers) {
            if (!streamer.writeQueueFull()) {// back-pressure
                streamer.write(buffer.copy());// buffers cannot be resused
            }
        }
    }

    private void openNextFile() {
        OpenOptions opts = new OpenOptions().setRead(true);
        String nextFile = playlist.poll();
        if (null != nextFile) {
            currentFile = vertx.fileSystem()
                    .openBlocking("tracks/" + nextFile, opts);
            positionInFile = 0;
        }
    }

    private void closeCurrentFile() {
        positionInFile = 0;
        currentFile.close();
        currentFile = null;
    }
}
