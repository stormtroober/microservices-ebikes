package infrastructure.adapters.eureka;

import application.ports.EurekaRegistrationPort;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EurekaRegistrationAdapter implements EurekaRegistrationPort {
    private static final Logger logger = LoggerFactory.getLogger(EurekaRegistrationAdapter.class);
    private final WebClient client;
    private final String eurekaHost;
    private final int eurekaPort;
    private final boolean eurekaEnabled;
    private String instanceId;
    private final Vertx vertx;

    public EurekaRegistrationAdapter(Vertx vertx, JsonObject config) {
        WebClientOptions options = new WebClientOptions()
                .setConnectTimeout(2000)
                .setIdleTimeout(30);
        this.vertx = vertx;
        this.client = WebClient.create(vertx, options);
        this.eurekaHost = config.getString("eurekaHost");
        this.eurekaPort = config.getInteger("eurekaPort");
        this.eurekaEnabled = config.getBoolean("eurekaEnabled");
    }

    @Override
    public Future<Void> register(String applicationName, String hostName, int port) {
        if (!eurekaEnabled) {
            return Future.succeededFuture();
        }

        this.instanceId = hostName + ":" + applicationName + ":" + port;

        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("instanceId", instanceId)
                        .put("hostName", hostName)
                        .put("app", applicationName)
                        .put("ipAddr", "127.0.0.1")
                        .put("vipAddress", applicationName)
                        .put("status", "UP")
                        .put("port", new JsonObject()
                                .put("$", port)
                                .put("@enabled", true))
                        .put("healthCheckUrl", "http://" + hostName + ":" + port + "/health")
                        .put("statusPageUrl", "http://" + hostName + ":" + port + "/info")
                        .put("homePageUrl", "http://" + hostName + ":" + port + "/")
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn")));

        logger.info("Registering with Eureka: " + instance.encodePrettily());
        return client.post(eurekaPort, eurekaHost, "/eureka/apps/" + applicationName)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(instance)
                .map(response -> {
                    if (response.statusCode() == 204) {
                        logger.info("Successfully registered with Eureka");
                        startHeartbeat(applicationName);
                        return null;
                    } else {
                        throw new RuntimeException("Failed to register with Eureka: " + response.statusCode());
                    }
                });
    }

    private void startHeartbeat(String applicationName) {
        this.vertx.setPeriodic(30000, id -> {
            sendHeartbeat(applicationName, instanceId)
                    .onFailure(err -> logger.warn("Failed to send heartbeat: {}", err.getMessage()));
        });
    }

    @Override
    public Future<Void> sendHeartbeat(String applicationName, String instanceId) {
        if (!eurekaEnabled) {
            return Future.succeededFuture();
        }

        return client.put(eurekaPort, eurekaHost, "/eureka/apps/" + applicationName + "/" + instanceId)
                .send()
                .map(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 404) {
                        if (response.statusCode() == 404) {
                            logger.info("Instance not found, re-registering...");
                            register(applicationName, instanceId.split(":")[0], Integer.parseInt(instanceId.split(":")[2]));
                        }
                        return null;
                    } else {
                        throw new RuntimeException("Failed to send heartbeat: " + response.statusCode());
                    }
                });
    }

    @Override
    public Future<Void> deregister(String applicationName, String instanceId) {
        if (!eurekaEnabled) {
            return Future.succeededFuture();
        }

        return client.delete(eurekaPort, eurekaHost, "/eureka/apps/" + applicationName + "/" + instanceId)
                .send()
                .map(response -> {
                    if (response.statusCode() == 200) {
                        logger.info("Successfully deregistered from Eureka");
                        return null;
                    } else {
                        throw new RuntimeException("Failed to deregister from Eureka: " + response.statusCode());
                    }
                });
    }
}