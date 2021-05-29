package com.wolf.inaction.web;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Description:
 * Created on 2021/5/29 6:45 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class CryptoHelper {
    static String publicKey() throws IOException {
        return read("public_key.pem");
    }

    static String privateKey() throws IOException {
        return read("private_key.pem");
    }

    private static String read(String file) throws IOException {
        URL resource = CryptoHelper.class.getResource("/public-api/" + file);
        Path path = Paths.get(resource.getPath());
        if (!path.toFile().exists()) {
            path = Paths.get("..", "public-api", file);
        }
        return String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        //Path path = Paths.get("/public-api", file);
        //if (!path.toFile().exists()) {
        //    path = Paths.get("..", "public-api", file);
        //}
    }
}
