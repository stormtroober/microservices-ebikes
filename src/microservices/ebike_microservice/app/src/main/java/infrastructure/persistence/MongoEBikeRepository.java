package infrastructure.persistence;

import application.ports.EBikeRepository;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.mongo.MongoClient;

import java.util.concurrent.CompletableFuture;

public class MongoEBikeRepository implements EBikeRepository {
    private final MongoClient mongoClient;
    private final String COLLECTION = "ebikes";

    public MongoEBikeRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public CompletableFuture<Void> save(JsonObject ebike) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Transform the input JsonObject to MongoDB format
        JsonObject document = new JsonObject()
                .put("_id", ebike.getString("id"))
                .put("state", ebike.getString("state"))
                .put("batteryLevel", ebike.getInteger("batteryLevel"))
                .put("location", ebike.getJsonObject("location"));

        mongoClient.insert(COLLECTION, document)
                .onSuccess(result -> future.complete(null))
                .onFailure(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<Void> update(JsonObject ebike) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        JsonObject query = new JsonObject().put("_id", ebike.getString("id"));
        JsonObject update = new JsonObject().put("$set", new JsonObject()
                .put("state", ebike.getString("state"))
                .put("batteryLevel", ebike.getInteger("batteryLevel"))
                .put("location", ebike.getJsonObject("location")));

        mongoClient.updateCollection(COLLECTION, query, update)
                .onSuccess(result -> future.complete(null))
                .onFailure(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<JsonObject> findById(String id) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        JsonObject query = new JsonObject().put("_id", id);

        mongoClient.findOne(COLLECTION, query, null)
                .onSuccess(result -> {
                    if (result != null) {
                        // Transform MongoDB document to domain format
                        JsonObject ebike = new JsonObject()
                                .put("id", result.getString("_id"))
                                .put("state", result.getString("state"))
                                .put("batteryLevel", result.getInteger("batteryLevel"))
                                .put("location", result.getJsonObject("location"));
                        future.complete(ebike);
                    } else {
                        future.complete(null);
                    }
                })
                .onFailure(future::completeExceptionally);

        return future;
    }

    @Override
    public CompletableFuture<JsonArray> findAll() {
        CompletableFuture<JsonArray> future = new CompletableFuture<>();
        JsonObject query = new JsonObject();

        mongoClient.find(COLLECTION, query)
                .onSuccess(results -> {
                    JsonArray ebikes = new JsonArray();
                    results.forEach(result -> {
                        JsonObject ebike = new JsonObject()
                                .put("id", result.getString("_id"))
                                .put("state", result.getString("state"))
                                .put("batteryLevel", result.getInteger("batteryLevel"))
                                .put("location", result.getJsonObject("location"));
                        ebikes.add(ebike);
                    });
                    future.complete(ebikes);
                })
                .onFailure(future::completeExceptionally);

        return future;
    }
}
