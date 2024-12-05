package infrastructure.adapters.web;

import application.ports.EurekaRegistrationPort;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EBikeVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(EBikeVerticle.class);

    private final RESTEBikeAdapter controller;
    private final EurekaRegistrationPort eurekaRegistration;
    private final String applicationName;
    private final String hostName;
    private final int port;

    public EBikeVerticle(
            RESTEBikeAdapter controller,
            EurekaRegistrationPort eurekaRegistration,
            JsonObject config
    ) {
        this.controller = controller;
        this.eurekaRegistration = eurekaRegistration;
        this.applicationName = config.getString("hostName");
        this.hostName = config.getString("hostName");
        this.port = config.getInteger("port");
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Configure routes
        controller.configureRoutes(router);

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(server -> {
                    registerWithEureka();
                    logger.info("HTTP server started on port {}", port);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    private void registerWithEureka() {
        try {

            eurekaRegistration.register(applicationName, hostName, port)
                    .onSuccess(v -> logger.info("Successfully registered with Eureka"))
                    .onFailure(err -> logger.error("Failed to register with Eureka", err));
        } catch (Exception e) {
            logger.error("Failed to get hostname", e);
        }
    }



}
