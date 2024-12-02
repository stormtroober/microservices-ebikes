package org.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;

public class UserVerticle extends AbstractVerticle {

    private final WebClient webClient;
    private final HttpClient httpClient;
    private WebSocket userWebSocket;
    private WebSocket bikeWebSocket;
    private WebSocket rideWebSocket;
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
                ws.writeTextMessage("i'm online", write -> {
                    if (write.succeeded()) {
                        System.out.println("Message sent successfully");
                    } else {
                        System.out.println("Failed to send message: " + write.cause().getMessage());
                    }
                });
                ws.textMessageHandler(this::handleUserUpdate);
                ws.exceptionHandler(err -> {
                    System.out.println("WebSocket error: " + err.getMessage());
                });
            }).onFailure(err -> {
                System.out.println("Failed to connect to user updates WebSocket: " + err.getMessage());
            });

        httpClient.webSocket(8081, "localhost", "/MAP-MICROSERVICE/observeUserBikes")
            .onSuccess(ws -> {
                System.out.println("Connected to user bikes updates WebSocket");
                bikeWebSocket = ws;
                ws.textMessageHandler(this::handleBikeUpdate);

                webClient.get(8081, "localhost", "/EBIKE-MICROSERVICE/api/ebikes")
                    .send(ar -> {
                        if (ar.succeeded() && ar.result().statusCode() == 200) {
                            ar.result().bodyAsJsonArray().forEach(bike -> {
                                handleBikeUpdate(bike.toString());
                            });
                        } else {
                            System.out.println("Failed to fetch bikes: " +
                                (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
                        }
                    });
            });


    }

    private void handleUserUpdate(String message) {
        System.out.println("[handleUserUpdate] Raw message: " + message);
        System.out.println("Received user update raw message: " + message); // Log
        try {
            JsonObject update = new JsonObject(message);
            String username = update.getString("username");
            System.out.println("Processed user update: " + update.encodePrettily());
            vertx.eventBus().publish("user.update." + username, update);
        } catch (Exception e) {
            System.err.println("Error processing user update: " + e.getMessage());
        }
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

        vertx.eventBus().consumer("user.ride.start", message -> {
            System.out.println("Received start ride request: " + message.body());
            rideWebSocket.writeTextMessage(message.body().toString());
        });

        vertx.eventBus().consumer("user.update.recharge", message -> {
            JsonObject creditDetails = (JsonObject) message.body();
            webClient.put(8081, "localhost", "/USER-MICROSERVICE/api/users/" + username + "recharge")
                .sendJsonObject(creditDetails, ar -> {
                    if (ar.succeeded() && ar.result().statusCode() == 200) {
                        message.reply(ar.result().bodyAsJsonObject());
                    } else {
                        message.fail(500, "Failed to recharge credit: " +
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
        if (rideWebSocket != null) {
            rideWebSocket.close();
        }
        if (bikeWebSocket != null) {
            bikeWebSocket.close();
        }

    }
}
