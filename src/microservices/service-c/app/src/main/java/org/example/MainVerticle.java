package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    private String serviceName;
    private int port;

    @Override
    public void start(Promise<Void> startPromise) {
        int port = Integer.parseInt(System.getenv("SERVICE_C_PORT"));
        String serviceName = System.getenv("SERVICE_C_NAME");

        Router router = Router.router(vertx);
        router.get("/").handler(ctx -> {
            ctx.response().end("Bau from " + serviceName);
        });

        // Connect to Service B WebSocket
        String serviceBHost = System.getenv("SERVICE_B_NAME"); // e.g., "localhost"
        int serviceBPort = Integer.parseInt(System.getenv("SERVICE_B_PORT"));
        String webSocketPath = "/websocket";

        vertx.createHttpClient().webSocket(serviceBPort, serviceBHost, webSocketPath, result -> {
            if (result.succeeded()) {
                WebSocket ws = result.result();
                System.out.println("Connected to WebSocket at Service B");

                vertx.setPeriodic(5000, id -> {
                    ws.writeTextMessage("Bau Bau");
                });

                // Handle incoming messages
                ws.textMessageHandler(msg -> {
                    System.out.println("[WebSocket] [" + serviceName + "] Received: " + msg);
                });

                // Handle connection close
                ws.closeHandler(v -> {
                    System.out.println("WebSocket closed");
                });

                // Handle errors
                ws.exceptionHandler(err -> {
                    System.err.println("WebSocket error: " + err.getMessage());
                });
            } else {
                System.err.println("Failed to connect to WebSocket: " + result.cause());
            }
        });

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        System.out.println(serviceName + " started on port " + port);
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment muszu id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
            }
        });
    }
}