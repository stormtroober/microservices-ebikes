package application;

import application.ports.EBikeRepository;
import application.ports.EBikeServiceAPI;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.CompletableFuture;

public class EBikeServiceImpl implements EBikeServiceAPI {

    private final EBikeRepository repository;

    public EBikeServiceImpl(EBikeRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompletableFuture<JsonObject> createEBike(
        String id,
        float x,
        float y
    ) {
        JsonObject ebike = new JsonObject()
            .put("id", id)
            .put("state", "AVAILABLE")
            .put("batteryLevel", 100)
            .put("location", new JsonObject().put("x", x).put("y", y));

        return repository.save(ebike).thenApply(v -> ebike);
    }

    @Override
    public CompletableFuture<JsonObject> getEBike(String id) {
        return repository.findById(id);
    }

    @Override
    public CompletableFuture<JsonObject> rechargeEBike(String id) {
        return repository
            .findById(id)
            .thenCompose(ebike -> {
                if (ebike != null) {
                    ebike.put("batteryLevel", 100).put("state", "AVAILABLE");
                    return repository.update(ebike).thenApply(v -> ebike);
                }
                return CompletableFuture.completedFuture(null);
            });
    }

    @Override
    public CompletableFuture<JsonObject> updateEBike(JsonObject ebike) {
        return repository
            .findById(ebike.getString("id"))
            .thenCompose(existingEBike -> {
                if (existingEBike != null) {
                    // Apply business rules
                    if (ebike.containsKey("batteryLevel")) {
                        int newBattery = ebike.getInteger("batteryLevel");
                        int currentBattery = existingEBike.getInteger(
                            "batteryLevel"
                        );
                        if (newBattery < currentBattery) {
                            existingEBike.put("batteryLevel", newBattery);
                            if (newBattery == 0) {
                                existingEBike.put("state", "MAINTENANCE");
                            }
                        }
                    }
                    if (ebike.containsKey("state")) {
                        existingEBike.put("state", ebike.getString("state"));
                    }
                    if (ebike.containsKey("location")) {
                        existingEBike.put(
                            "location",
                            ebike.getJsonObject("location")
                        );
                    }

                    return repository
                        .update(existingEBike)
                        .thenApply(v -> existingEBike);
                }
                return CompletableFuture.completedFuture(null);
            });
    }

    @Override
    public CompletableFuture<JsonArray> getAllEBikes() {
        return repository.findAll();
    }
}
