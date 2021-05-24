package com.wolf.inaction.echo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Synchronous echo TCP protocol
// 主线程接收连接，每个连接用一个线程处理I/O，I/O操作是同步的，线程可能阻塞在I/O操作上
// 每个连接一个线程，线程是重资源。很多线程需要调度。

// netcat localhost 3000
// aa
// /quit
public class SynchronousEcho {
  public static void main(String[] args) throws IOException {
    ServerSocket server = new ServerSocket();
    server.bind(new InetSocketAddress(3000));
    while (true) {
      Socket socket = server.accept();// block when no connection is pending
      new Thread(clientHandler(socket)).start();
    }
  }

  private static Runnable clientHandler(Socket socket) {
    return () -> {
      try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
        String line = "";
        while (!"/quit".equals(line)) {
          line = reader.readLine();// 可能阻塞，由于没有足够数据而阻塞
          System.out.println("~ " + line);
          writer.write(line + "\n");// 可能阻塞，例如tcp的buffer有空间
          writer.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
  }
}
