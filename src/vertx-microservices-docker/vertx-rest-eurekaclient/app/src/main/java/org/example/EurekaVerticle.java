package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.TimeUnit;

public class EurekaVerticle extends AbstractVerticle {

    private final String applicationName;
    private final String instanceId;
    private final int port;
    private WebClient client;

    public EurekaVerticle(String applicationName, String instanceId, int port) {
        this.applicationName = applicationName;
        this.instanceId = instanceId;
        this.port = port;
    }

    @Override
    public void start() {
        client = WebClient.create(vertx);

        // Create a router object
        Router router = Router.router(vertx);

        // Health check endpoint
        router.get("/health").handler(ctx -> {
            ctx.response()
                    .putHeader("content-type", "application/json")
                    .end("{\"status\":\"UP\"}");
        });

        // Define the info endpoint
        router.get("/info").handler(ctx -> {
            ctx.response().putHeader("content-type", "application/json").end(new JsonObject().put("info", "Vert.x Service").encode());
        });

        // Define the default endpoint
        router.get("/").handler(ctx -> {
            ctx.response().end("Vert.x Service is running");
        });

        // Start the Vert.x server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        System.out.println("HTTP server started on port " + port);

                        //Register with Eureka after the HTTP server has started
                        registerWithEureka();

                        //Start sending heartbeats
                        vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), id -> sendHeartbeat());
                    } else {
                        System.out.println("Failed to start HTTP server: " + http.cause());
                    }
                });
    }

    private void registerWithEureka() {
        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("hostName", "vertx-rest-eurekaclient")  // Update this line
                        .put("app", applicationName)
                        .put("instanceId", instanceId)
                        .put("ipAddr", "127.0.0.1")
                        .put("vipAddress", "vertx-service")
                        .put("port", new JsonObject().put("$", port).put("@enabled", true))
                        .put("status", "UP")
                        .put("healthCheckUrl", "http://vertx-rest-eurekaclient:" + port + "/health")  // Update this line
                        .put("statusPageUrl", "http://vertx-rest-eurekaclient:" + port + "/info")  // Update this line
                        .put("homePageUrl", "http://vertx-rest-eurekaclient:" + port + "/")  // Update this line
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn")));

        client.post(8761, "eureka-server", "/eureka/apps/" + applicationName)
                .sendJsonObject(instance, res -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully registered with Eureka");
                    } else {
                        System.err.println("Failed to register with Eureka: " + res.cause());
                    }
                });
    }

    private void sendHeartbeat() {
        client.put(8761, "eureka-server", "/eureka/apps/" + applicationName + "/" + instanceId)
                .send(res -> {
                    if (res.succeeded()) {
                        System.out.println("Heartbeat sent successfully");
                    } else {
                        System.err.println("Failed to send heartbeat: " + res.cause());
                    }
                });
    }

}