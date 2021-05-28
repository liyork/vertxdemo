package com.wolf.inaction.asyndata_eventstream;

import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Description:
 * Created on 2021/5/26 6:41 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class ReadFile {
    public static void main(String[] args) {
        //readFileJavaIO();
        readFileVertx();
    }

    private static void readFileJavaIO() {
        URL resource = ReadFile.class.getClassLoader().getResource("a.txt");
        File file = new File(resource.getPath());
        byte[] buffer = new byte[1024];
        try (FileInputStream in = new FileInputStream(file)) {// try-with-resources
            int count = in.read(buffer);// being pulling data
            while (count != -1) {
                System.out.println(new String(buffer, 0, count));
                count = in.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("\n--- DONE");
        }
    }

    private static void readFileVertx() {
        Vertx vertx = Vertx.vertx();
        OpenOptions opts = new OpenOptions().setRead(true);
        URL resource = ReadFile.class.getClassLoader().getResource("a.txt");
        // 异步
        vertx.fileSystem().open(resource.getPath(), opts, ar -> {
            if (ar.succeeded()) {// being pushed data
                AsyncFile file = ar.result();
                file.handler(System.out::println)// callback for new buffer data
                        .exceptionHandler(Throwable::printStackTrace)// when exception arise
                        .endHandler(done -> {// when the stream end
                            System.out.println("\n--- DONE");
                            vertx.close();
                        });
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
