package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        int port = Integer.parseInt(System.getenv("SERVICE_PORT"));
        String serviceName = System.getenv("SERVICE_NAME");

        Router router = Router.router(vertx);
        router.get("/").handler(ctx -> {
            ctx.response().end("Hello from " + serviceName);
        });

        // Basic inter-service request example
        vertx.setPeriodic(5000, id -> {
//            makeRequest("service-b", 8082);
//            makeRequest("service-c", 8083);
        });

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

//    public void makeRequest(String targetHost, int targetPort) {
//        vertx.createHttpClient().get(targetPort, targetHost, "/", response -> {
//            response.handler(body -> {
//                System.out.println("Received response from " + targetHost + ": " + body.toString());
//            });
//        }).end();
//    }

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