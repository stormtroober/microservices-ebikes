package org.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;

public class UserVerticle extends AbstractVerticle {

    private final WebClient webClient;
    private final HttpClient httpClient;
    private WebSocket userWebSocket;
    private WebSocket bikeWebSocket;
    private final Vertx vertx;
    private final String username;

    public UserVerticle(Vertx vertx, String username) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.httpClient = vertx.createHttpClient();
        this.username = username;
    }

    public void init() {
        vertx.deployVerticle(this).onSuccess(id -> {
            System.out.println("UserVerticle deployed successfully with ID: " + id);
            setupWebSocketConnections();
        }).onFailure(err -> {
            System.out.println("Failed to deploy UserVerticle: " + err.getMessage());
        });
    }

    private void setupWebSocketConnections() {
      // Connect to user updates WebSocket
        httpClient.webSocket(8081, "localhost", "/USER-MICROSERVICE/observeUser/" + username)
            .onSuccess(ws -> {
                System.out.println("Connected to user updates WebSocket: " + username);
                userWebSocket = ws;
                ws.textMessageHandler(this::handleUserUpdate);
                ws.exceptionHandler(err -> {
                    System.out.println("WebSocket error: " + err.getMessage());
                });
            }).onFailure(err -> {
                System.out.println("Failed to connect to user updates WebSocket: " + err.getMessage());
            });

        httpClient.webSocket(8081, "localhost", "/MAP-MICROSERVICE/observeUserBikes?username=" + username)
            .onSuccess(ws -> {
                System.out.println("Connected to user bikes updates WebSocket");
                bikeWebSocket = ws;
                ws.textMessageHandler(message ->{
                        System.out.println("Received bike update: " + message);
                        vertx.eventBus().publish("user.bike.update."+username, new JsonArray(message));
                });


            });


    }

    private void handleUserUpdate(String message) {
        JsonObject update = new JsonObject(message);
        String username = update.getString("username");
        System.out.println("Processed user update: " + update.encodePrettily());
        vertx.eventBus().publish("user.update." + username, update);
    }

    private void handleRideUpdate(String message) {
        System.out.println("Received ride update: " + message);
    }


    private void handleBikeUpdate(String message) {
        JsonObject update = new JsonObject(message);
        //System.out.println("Received bike update: " + message);
        vertx.eventBus().publish("user.bike.update", update);
    }

    @Override
    public void start() {

        vertx.eventBus().consumer("user.update.recharge" + username, message -> {
            JsonObject creditDetails = (JsonObject) message.body();
            System.out.println("Recharging credit: " + creditDetails.encodePrettily());
            webClient.patch(8081, "localhost", "/USER-MICROSERVICE/api/users/" + username + "/recharge")
                .sendJsonObject(creditDetails, ar -> {
                    if (ar.succeeded() && ar.result().statusCode() == 200) {
                        System.out.println("Credit recharged successfully");
                        message.reply(ar.result().bodyAsJsonObject());
                    } else {
                        message.fail(500, "Failed to recharge credit: " +
                            (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
                    }
                });
        });

        vertx.eventBus().consumer("user.ride.start." + username, message -> {
            JsonObject rideDetails = (JsonObject) message.body();
            System.out.println("Starting ride: " + rideDetails.encodePrettily());
            webClient.post(8081, "localhost", "/RIDE-MICROSERVICE/startRide")
                .sendJsonObject(rideDetails, ar -> {
                    if (ar.succeeded() && ar.result().statusCode() == 200) {
                        System.out.println("Ride started successfully");
                        message.reply(ar.result().bodyAsJsonObject());
                    } else {
                        message.fail(500, "Failed to start ride: " +
                            (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
                    }
                });
        });

        vertx.eventBus().consumer("user.ride.stop." + username, message -> {
            JsonObject rideDetails = (JsonObject) message.body();
            System.out.println("Stopping ride: " + rideDetails.encodePrettily());
            webClient.post(8081, "localhost", "/RIDE-MICROSERVICE/stopRide")
                .sendJsonObject(rideDetails, ar -> {
                    if (ar.succeeded() && ar.result().statusCode() == 200) {
                        System.out.println("Ride stopped successfully");
                        message.reply(ar.result().bodyAsJsonObject());
                    } else {
                        message.fail(500, "Failed to stop ride: " +
                            (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
                    }
                });
        });
    }

    @Override
    public void stop() {
        if (userWebSocket != null) {
            userWebSocket.close();
        }
        if (bikeWebSocket != null) {
            bikeWebSocket.close();
        }

    }
}
