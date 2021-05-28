package com.wolf.inaction.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Description:
 * OP_ACCEPT
 * ->OP_READ->OP_WRITE->OP_READ->OP_WRITE...
 * ->OP_READ->OP_WRITE->OP_READ->OP_WRITE...
 * 事件注册到Selector上
 * <p>
 * netcat localhost 3000
 * aa
 * /quit
 * Created on 2021/5/23 5:32 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class AsynchronousEcho {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(3000));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);// selector will notify of incoming connections

        // main loop
        while (true) {
            selector.select();// collects all non-blocking I/O notifications
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isAcceptable()) {// a new connection
                    newConnection(selector, key);
                } else if (key.isReadable()) {// a socket has received data
                    echo(key);
                } else if (key.isWritable()) {// a socket is ready for writing
                    continueEcho(selector, key);
                }
                it.remove();// 手动删除selectedKey，否则下次循环时还是可用的
            }
        }
    }

    // 上下文依赖于应用和协议
    // 保持tcp连接上的处理状态(这里是当前行和是否连接正在关闭，还有buffer)
    private static class Context {
        private final ByteBuffer nioBuffer = ByteBuffer.allocate(512);
        private String currentLine = "";
        private boolean terminating = false;
    }

    private static final HashMap<SocketChannel, Context> contexts = new HashMap<>();

    // 处理新建连
    private static void newConnection(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();// 新连接
        socketChannel.configureBlocking(false).register(selector, SelectionKey.OP_READ);// 新连接非阻塞并感兴趣读
        contexts.put(socketChannel, new Context());
    }

    private static final Pattern QUIT = Pattern.compile("(\\r)?(\\n)?/quit$");

    // 这里看来，除非写回的内容很多，要不会一直保持在OP_READ
    private static void echo(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Context context = contexts.get(socketChannel);
        try {
            socketChannel.read(context.nioBuffer);
            context.nioBuffer.flip();
            context.currentLine = context.currentLine + Charset.defaultCharset().decode(context.nioBuffer);
            if (QUIT.matcher(context.currentLine).find()) {// 退出/quit，终止连接
                context.terminating = true;
            } else if (context.currentLine.length() > 16) {// 太长?
                context.currentLine = context.currentLine.substring(8);
            }
            context.nioBuffer.flip();// buffer中已经有数据，需要写回到client，需要flip返回到开始位置
            int count = socketChannel.write(context.nioBuffer);
            // 理论应该是写入count=limit，若是小于表示本次没有写全，那就是buffer没地方可写了。
            // 那就不再继续这样的操作(读取，写回)
            // 停止更多的读操作(因为会一直读取直到cancel+register新的)，并对可写事件感兴趣。
            if (count < context.nioBuffer.limit()) {
                System.out.println("echo, count < context.nioBuffer.limit()");
                key.cancel();
                socketChannel.register(key.selector(), SelectionKey.OP_WRITE);
            } else {// 正常写回，清空buffer
                System.out.println("echo, count == context.nioBuffer.limit()");
                context.nioBuffer.clear();
                if (context.terminating) {
                    cleanup(socketChannel);
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
            cleanup(socketChannel);
        }
    }

    private static void cleanup(SocketChannel socketChannel) throws IOException {
        socketChannel.close();
        contexts.remove(socketChannel);
    }

    private static void continueEcho(Selector selector, SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Context context = contexts.get(socketChannel);
        try {
            int remainingBytes = context.nioBuffer.limit() - context.nioBuffer.position();
            int count = socketChannel.write(context.nioBuffer);
            // 这里等于应该是都写入了，怎么下面还cancel?
            // 一直是这种状态，直到所有数据被写回。然后取消write兴趣并声明对读感兴趣
            if (count == remainingBytes) {
                System.out.println("continueEcho, count == remainingBytes");
                context.nioBuffer.clear();
                key.cancel();
                if (context.terminating) {
                    cleanup(socketChannel);
                } else {
                    System.out.println("continueEcho, not terminating");
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
            cleanup(socketChannel);
        }
    }
}

