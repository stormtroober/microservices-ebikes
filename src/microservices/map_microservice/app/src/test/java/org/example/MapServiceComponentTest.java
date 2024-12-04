package org.example;

import application.RestMapServiceAPIImpl;
import application.ports.EventPublisher;
import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeState;
import domain.model.P2d;
import infrastructure.EBikeRepositoryImpl;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import infrastructure.adapter.BikeUpdateAdapter;
import infrastructure.adapter.MapServiceVerticle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class MapServiceComponentTest {

    private Vertx vertx;
    private WebClient client;
    private RestMapServiceAPI mapService;
    private MockEventPublisher eventPublisher;
    private static final int BIKE_UPDATE_PORT = 8088;

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
        eventPublisher = new MockEventPublisher();

        // Initialize components
        EBikeRepositoryImpl repository = new EBikeRepositoryImpl();
        mapService = new RestMapServiceAPIImpl(repository, eventPublisher);

        // Deploy BikeUpdateAdapter only for this test
        vertx.deployVerticle(new BikeUpdateAdapter(mapService))
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        // Add delay to ensure server is ready
                        vertx.setTimer(1000, id -> {
                            System.out.println("BikeUpdateAdapter deployed successfully");
                            testContext.completeNow();
                        });
                    } else {
                        System.err.println("Failed to deploy BikeUpdateAdapter: " + ar.cause());
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        vertx.close().onComplete(testContext.succeeding(v -> testContext.completeNow()));
    }

    @Test
    void testUpdateEBike(VertxTestContext testContext) {
        JsonObject bikeJson = new JsonObject()
                .put("id", "bike1")
                .put("location", new JsonObject()
                        .put("x", 10.0)
                        .put("y", 20.0))
                .put("state", "AVAILABLE")
                .put("batteryLevel", 100);

        System.out.println("Sending request to update bike: " + bikeJson.encodePrettily());

        client.put(BIKE_UPDATE_PORT, "localhost", "/updateEBike")
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(bikeJson)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<Buffer> response = ar.result();
                        System.out.println("Response status code: " + response.statusCode());
                        System.out.println("Response body: " + response.bodyAsString());

                        testContext.verify(() -> {
                            assertEquals(200, response.statusCode());
                            testContext.completeNow();
                        });
                    } else {
                        System.err.println("Request failed: " + ar.cause().getMessage());
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @Test
    void testErrorHandling(VertxTestContext testContext) {
        JsonObject invalidBikeJson = new JsonObject()
                .put("id", "") // Invalid empty ID
                .put("location", new JsonObject()
                        .put("x", 10.0)
                        .put("y", 20.0))
                .put("state", "INVALID_STATE")
                .put("batteryLevel", -1);

        client.put(BIKE_UPDATE_PORT, "localhost", "/updateEBike")
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(invalidBikeJson)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<Buffer> response = ar.result();
                        System.out.println("Error response status code: " + response.statusCode());
                        System.out.println("Error response body: " + response.bodyAsString());

                        testContext.verify(() -> {
                            assertEquals(400, response.statusCode());
                            testContext.completeNow();
                        });
                    } else {
                        System.err.println("Request failed: " + ar.cause().getMessage());
                        testContext.failNow(ar.cause());
                    }
                });
    }

    private static class MockEventPublisher implements EventPublisher {
        private final List<EBike> lastPublishedBikes = new ArrayList<>();
        private boolean userBikesUpdateCalled = false;

        @Override
        public void publishBikesUpdate(List<EBike> bikes) {
            System.out.println("Publishing bikes update: " + bikes);
            lastPublishedBikes.clear();
            lastPublishedBikes.addAll(bikes);
        }

        @Override
        public void publishUserBikesUpdate(List<EBike> bikes, String username) {
            System.out.println("Publishing user bikes update for " + username + ": " + bikes);
            userBikesUpdateCalled = true;
            lastPublishedBikes.clear();
            lastPublishedBikes.addAll(bikes);
        }

        @Override
        public void publishUserAvailableBikesUpdate(List<EBike> bikes) {
            System.out.println("Publishing available bikes update: " + bikes);
            lastPublishedBikes.clear();
            lastPublishedBikes.addAll(bikes);
        }

        @Override
        public void publishStopRide(String username) {

        }

        public List<EBike> getLastPublishedBikes() {
            return new ArrayList<>(lastPublishedBikes);
        }

        public boolean wasUserBikesUpdateCalled() {
            return userBikesUpdateCalled;
        }
    }
}