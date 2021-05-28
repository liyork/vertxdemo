package com.wolf.inaction.firstapp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

/**
 * Description:
 * an event loop is managing the processing of events,
 * be it a new TCP connection, the arrival of a buffer, na new HTTP request, or a periodic task.
 * every event handler is being executed on the same(event-loop) thread
 * <p>
 * netcat localhost 3000
 * curl localhost:8080
 * <p>
 * wrk --latency http://localhost:8080
 * Created on 2021/5/24 12:52 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class VertxEcho {
    private static int numberOfConnections = 0;// event handlers are always executed on the same thread, no need lock

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.createNetServer()
                .connectHandler(VertxEcho::handleNewClient)// passing a cllback for each new connection
                .listen(3000);

        vertx.setPeriodic(5000, id -> System.out.println(howMany()));// periodic task

        vertx.createHttpServer()
                .requestHandler(request -> request.response().end(howMany()))// be executed for each http request
                .listen(8080);
    }

    private static void handleNewClient(NetSocket socket) {
        numberOfConnections++;
        socket.handler(buffer -> {// is invoked every time a buffer is ready for consumption
            socket.write(buffer);
            if (buffer.toString().endsWith("/quit\n")) {
                socket.close();
            }
        });
        socket.closeHandler(v -> numberOfConnections--);// event is when the connection close
    }

    private static String howMany() {
        return "We now have " + numberOfConnections + " connections";
    }
}
