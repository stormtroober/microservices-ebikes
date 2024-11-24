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
    private Vertx vertx;

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

        this.instanceId = "localhost:" + applicationName + ":" + port;

        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("instanceId", instanceId)
                        .put("hostName", hostName)
                        .put("app", applicationName.toUpperCase())
                        .put("ipAddr", hostName)
                        .put("status", "UP")
                        .put("port", new JsonObject()
                                .put("$", port)
                                .put("@enabled", true))
                        .put("securePort", new JsonObject()
                                .put("$", 443)
                                .put("@enabled", false))
                        .put("healthCheckUrl", "http://" + hostName + ":" + port + "/health")
                        .put("statusPageUrl", "http://" + hostName + ":" + port + "/info")
                        .put("homePageUrl", "http://" + hostName + ":" + port + "/")
                        .put("vipAddress", applicationName.toLowerCase())
                        .put("secureVipAddress", applicationName.toLowerCase())
                        .put("countryId", 1)
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn"))
                        .put("leaseInfo", new JsonObject()
                                .put("renewalIntervalInSecs", 30)
                                .put("durationInSecs", 90)
                                .put("registrationTimestamp", 0)
                                .put("lastRenewalTimestamp", 0)
                                .put("evictionTimestamp", 0)
                                .put("serviceUpTimestamp", 0))
                        .put("metadata", new JsonObject()
                                .put("management.port", String.valueOf(port))));

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

        return client.put(eurekaPort, eurekaHost,
                        "/eureka/apps/" + applicationName + "/" + instanceId)
                .send()
                .map(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 404) {
                        if (response.statusCode() == 404) {
                            // Re-register if instance not found
                            logger.info("Instance not found, re-registering...");
                            register(applicationName, instanceId.split(":")[0],
                                    Integer.parseInt(instanceId.split(":")[2]));
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

        return client.delete(eurekaPort, eurekaHost,
                        "/eureka/apps/" + applicationName + "/" + instanceId)
                .send()
                .map(response -> null);
    }
}