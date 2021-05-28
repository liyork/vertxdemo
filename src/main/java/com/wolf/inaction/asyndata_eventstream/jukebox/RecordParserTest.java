package com.wolf.inaction.asyndata_eventstream.jukebox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Description:
 * Created on 2021/5/26 1:44 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class RecordParserTest extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RecordParserTest.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //writeFile(vertx);
        //readFile(vertx);
        readFileWithFetch(vertx);
    }

    public static void writeFile(Vertx vertx) {
        AsyncFile file = vertx.fileSystem().openBlocking("sample.db",
                new OpenOptions().setWrite(true).setCreate(true));
        Buffer buffer = Buffer.buffer();

        buffer.appendBytes(new byte[]{1, 2, 3, 4});// magic number
        buffer.appendInt(2);// version
        buffer.appendString("Sample database\n");// database name

        String key = "abc";// first entry
        String value = "123456-abcdef";
        buffer.appendInt(key.length())
                .appendString(key)
                .appendInt(value.length())
                .appendString(value);

        key = "foo@bar";// second entry
        value = "Foo Bar Baz";
        buffer.appendInt(key.length())
                .appendString(key)
                .appendInt(value.length())
                .appendString(value);

        file.end(buffer, ar -> {
            vertx.close();
        });
    }

    public static void readFile(Vertx vertx) {
        URL resource = RecordParserTest.class.getClassLoader().getResource("sample.db");
        AsyncFile file = vertx.fileSystem().openBlocking(resource.getPath(),
                new OpenOptions().setRead(true));

        // 先设定模式，然后handler获取
        RecordParser parser = RecordParser.newFixed(4, file);// read magic number
        parser.handler(header -> readMagicNumber(header, parser));
        parser.endHandler(ar -> vertx.close());
    }

    private static void readMagicNumber(Buffer header, RecordParser parser) {
        logger.info("Magic number: {}:{}:{}:{}", header.getByte(0),
                header.getByte(1), header.getByte(2), header.getByte(3));
        parser.handler(version -> readVersion(version, parser));// 继续使用fix(4)
    }

    private static void readVersion(Buffer header, RecordParser parser) {
        logger.info("Version: {}", header.getInt(0));
        parser.delimitedMode("\n");// parser mode can be switched on the fly
        parser.handler(name -> readName(name, parser));
    }

    private static void readName(Buffer name, RecordParser parser) {
        logger.info("Name: {}", name.toString());
        parser.fixedSizeMode(4);// int
        parser.handler(keyLength -> readKey(keyLength, parser));
    }

    private static void readKey(Buffer keyLength, RecordParser parser) {
        parser.fixedSizeMode(keyLength.getInt(0));
        parser.handler(key -> readValue(key.toString(), parser));
    }

    private static void readValue(String key, RecordParser parser) {
        parser.fixedSizeMode(4);// int
        parser.handler(valueLength -> finishEntry(key, valueLength, parser));
    }

    private static void finishEntry(String key, Buffer valueLength, RecordParser parser) {
        parser.fixedSizeMode(valueLength.getInt(0));
        parser.handler(value -> {
            logger.info("Key: {} / Value: {}", key, value);
            parser.fixedSizeMode(4);
            parser.handler(keyLength -> readKey(keyLength, parser));
        });
    }

    public static void readFileWithFetch(Vertx vertx) {
        URL resource = RecordParserTest.class.getClassLoader().getResource("sample.db");
        AsyncFile file = vertx.fileSystem().openBlocking(resource.getPath(),
                new OpenOptions().setRead(true));

        RecordParser parser = RecordParser.newFixed(4, file);
        parser.pause();// the stream won't push events
        // 虽然handler是异步的，不过直接先设定fetch再用handler比较好
        parser.fetch(1);// ask for one element, ask for a buffer of four bytes
        parser.handler(header -> readMagicNumberWithFetch(header, parser));
        parser.endHandler(ar -> vertx.close());
    }

    private static void readMagicNumberWithFetch(Buffer header, RecordParser parser) {
        logger.info("Magic number: {}:{}:{}:{}", header.getByte(0),
                header.getByte(1), header.getByte(2), header.getByte(3));
        parser.fetch(1);
        parser.handler(version -> readVersionWithFetch(version, parser));
    }

    private static void readVersionWithFetch(Buffer header, RecordParser parser) {
        logger.info("Version: {}", header.getInt(0));
        parser.delimitedMode("\n");// parser mode can be switched on the fly
        parser.fetch(1);
        parser.handler(name -> readNameWithFetch(name, parser));
    }

    private static void readNameWithFetch(Buffer name, RecordParser parser) {
        logger.info("Name: {}", name.toString());
        parser.fixedSizeMode(4);// int
        parser.fetch(1);
        parser.handler(keyLength -> readKeyWithFetch(keyLength, parser));
    }

    private static void readKeyWithFetch(Buffer keyLength, RecordParser parser) {
        parser.fixedSizeMode(keyLength.getInt(0));
        parser.fetch(1);
        parser.handler(key -> readValueWithFetch(key.toString(), parser));
    }

    private static void readValueWithFetch(String key, RecordParser parser) {
        parser.fixedSizeMode(4);// int
        parser.fetch(1);
        parser.handler(valueLength -> finishEntryWithFetch(key, valueLength, parser));
    }

    private static void finishEntryWithFetch(String key, Buffer valueLength, RecordParser parser) {
        parser.fixedSizeMode(valueLength.getInt(0));
        parser.fetch(1);
        parser.handler(value -> {
            logger.info("Key: {} / Value: {}", key, value);
            parser.fixedSizeMode(4);
            parser.fetch(1);
            parser.handler(keyLength -> readKeyWithFetch(keyLength, parser));
        });
    }
}
