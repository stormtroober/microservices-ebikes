package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        int port = Integer.parseInt(System.getenv("SERVICE_A_PORT"));
        String serviceName = System.getenv("SERVICE_A_NAME");

        Router router = Router.router(vertx);
        router.get("/").handler(ctx -> {
            ctx.response().end("Bau Bau " + serviceName);
        });

        WebClient client = WebClient.create(vertx);

        // Basic inter-service request example
        vertx.setPeriodic(30000, id -> {
            makeRequest(client, System.getenv("SERVICE_B_NAME"), Integer.parseInt(System.getenv("SERVICE_B_PORT")));
            makeRequest(client, System.getenv("SERVICE_C_NAME"), Integer.parseInt(System.getenv("SERVICE_C_PORT")));
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

    private void makeRequest(WebClient client, String serviceName, int port) {
        System.out.println("Bau to " + serviceName + " from " + System.getenv("SERVICE_A_NAME"));
        client.get(port, serviceName, "/")
                .send()
                .onSuccess(response -> {
                    System.out.println("Bau response from " + serviceName + ": " + response.bodyAsString());
                })
                .onFailure(err -> {
                    System.err.println("Failed to Bau " + serviceName + ": " + err.getMessage());
                });
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