package infrastructure.config;

import io.vertx.core.json.JsonObject;

public class ApplicationConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_EUREKA_HOST = "localhost";
    private static final int DEFAULT_EUREKA_PORT = 8761;
    private static final String DEFAULT_MONGO_CONNECTION = "mongodb://localhost:27017";
    private static final String DEFAULT_DB_NAME = "ebikes_db";
    private static final boolean DEFAULT_EUREKA_ENABLED = true; // Disabled by default for local development
    private static final String DEFAULT_HOST_NAME = "ebike-microservice";

    private final String host;
    private final int port;
    private final String eurekaHost;
    private final int eurekaPort;
    private final String mongoConnection = System.getenv().getOrDefault(
            "MONGO_CONNECTION",
            "mongodb://mongodb-ebike:27017"  // Use Docker service name
    );
    private final String dbName;
    private final boolean eurekaEnabled;
    private final String hostName = System.getenv().getOrDefault("HOST_NAME", DEFAULT_HOST_NAME);

    public ApplicationConfig() {
        this.host = System.getenv().getOrDefault("SERVICE_HOST", DEFAULT_HOST);
        this.port = Integer.parseInt(System.getenv().getOrDefault("SERVICE_PORT", String.valueOf(DEFAULT_PORT)));
        this.eurekaHost = System.getenv().getOrDefault("EUREKA_HOST", "eureka-server");
        this.eurekaPort = Integer.parseInt(System.getenv().getOrDefault("EUREKA_PORT", String.valueOf(DEFAULT_EUREKA_PORT)));
        this.dbName = System.getenv().getOrDefault("DB_NAME", DEFAULT_DB_NAME);
        this.eurekaEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("EUREKA_ENABLED", String.valueOf(DEFAULT_EUREKA_ENABLED)));

    }
    public String getHostName() { return hostName; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getEurekaHost() { return eurekaHost; }
    public int getEurekaPort() { return eurekaPort; }
    public boolean isEurekaEnabled() { return eurekaEnabled; }

    public JsonObject getMongoConfig() {
        return new JsonObject()
                .put("connection_string", mongoConnection)
                .put("db_name", dbName);
    }

    public JsonObject getEurekaConfig() {
        return new JsonObject()
                .put("eurekaHost", eurekaHost)
                .put("eurekaPort", eurekaPort)
                .put("eurekaEnabled", eurekaEnabled);
    }
}