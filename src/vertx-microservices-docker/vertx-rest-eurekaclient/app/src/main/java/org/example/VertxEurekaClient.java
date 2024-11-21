package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class VertxEurekaClient {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);

        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("hostName", "localhost")
                        .put("app", "VERTX-SERVICE")
                        .put("ipAddr", "127.0.0.1")
                        .put("vipAddress", "vertx-service")
                        .put("port", new JsonObject().put("$", 8080).put("@enabled", true))
                        .put("status", "UP")
                        .put("healthCheckUrl", "http://localhost:8080/health")
                        .put("statusPageUrl", "http://localhost:8080/info")
                        .put("homePageUrl", "http://localhost:8080/")
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn")));

        // Register with Eureka
        client.post(8761, "eureka-server", "/eureka/apps/VERTX-SERVICE")
                .sendJsonObject(instance, res -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully registered with Eureka");
                    } else {
                        System.err.println("Failed to register with Eureka: " + res.cause());
                    }
                });

        // Start the Vert.x server
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Vert.x Service is running"))
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        System.out.println("HTTP server started on port 8080");
                    } else {
                        System.out.println("Failed to start HTTP server: " + http.cause());
                    }
                });
    }
}