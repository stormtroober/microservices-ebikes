package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class MainVerticle extends AbstractVerticle {

    private String serviceName;
    private int port;

    @Override
    public void start(Promise<Void> startPromise) {
        port = Integer.parseInt(System.getenv("SERVICE_B_PORT"));
        serviceName = System.getenv("SERVICE_B_NAME");


        Router router = Router.router(vertx);
        router.get("/").handler(ctx -> {
            ctx.response().end("Bau Bau " + serviceName);
        });


        vertx.createHttpServer()
                .requestHandler(req -> {
                    // Handle WebSocket upgrade requests
                    if (req.uri().equals("/websocket") && req.method().name().equals("GET")) {
                        req.toWebSocket().onSuccess(this::handleWebSocket).onFailure(err -> {
                            System.err.println("WebSocket handshake failed: " + err.getMessage());
                        });
                    } else {
                        // Handle regular HTTP requests
                        router.handle(req);
                    }
                })
                .listen(port, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        System.out.println(serviceName + " started on port " + port);
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void handleWebSocket(ServerWebSocket ws) {
        ws.frameHandler(frame -> {
            if (frame.isText()) {
                String receivedText = frame.textData();
                System.out.println("[WebSocket] [" + serviceName + "] Received: " + receivedText);

                // Echo the message back to the client
                ws.writeTextMessage("Bau Bau");
            }
        });

        ws.closeHandler(v -> System.out.println("WebSocket connection closed"));
        ws.exceptionHandler(err -> System.err.println("WebSocket error: " + err.getMessage()));
    }


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.out.println("Deployment failed!");
            }
        });
    }
}