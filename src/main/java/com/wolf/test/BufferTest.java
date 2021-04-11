package com.wolf.test;

import io.vertx.core.buffer.Buffer;

/**
 * Description:
 * Created on 2021/4/10 4:21 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class BufferTest {
  public static void main(String[] args) {
    Buffer buffer = Buffer.buffer();

    System.out.println("buffer.length = " + buffer.length());

    buffer.setByte(0, (byte) 127);
    buffer.setShort(2, (short) 127);
    buffer.setInt(4, 127);
    buffer.setLong(8, 127);
    buffer.setFloat(16, 127.0F);
    buffer.setDouble(20, 127.0D);

    buffer.appendByte((byte) 127);
    buffer.appendShort((short) 127);
    buffer.appendInt(127);
    buffer.appendLong(127);
    buffer.appendFloat(127.0F);
    buffer.appendDouble(127.0D);

    byte aByte = buffer.getByte(0);
    short aShort = buffer.getShort(2);
    int anInt = buffer.getInt(4);
    long aLong = buffer.getLong(8);
    float aFloat = buffer.getFloat(16);
    double aDouble = buffer.getDouble(20);

    System.out.println("buffer.length = " + buffer.length());
  }
}
