package infrastructure.persistence.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoConfig {
    public static MongoClient createClient(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://mongodb:27017")
                .put("db_name", "ebikes_db");

        return MongoClient.create(vertx, config);
    }
}
