package application.ports;

import io.vertx.core.Future;

/**
 * Interface for Eureka Registration Port.
 * Provides methods to register, send heartbeat, and deregister applications with Eureka.
 */
public interface EurekaRegistrationPort {

    /**
     * Registers an application instance with Eureka.
     *
     * @param applicationName the name of the application
     * @param hostName the host name of the application instance
     * @param port the port number of the application instance
     * @return a Future representing the result of the registration operation
     */
    Future<Void> register(String applicationName, String hostName, int port);

    /**
     * Sends a heartbeat to Eureka to keep the application instance registered.
     *
     * @param applicationName the name of the application
     * @param instanceId the unique identifier of the application instance
     * @return a Future representing the result of the heartbeat operation
     */
    Future<Void> sendHeartbeat(String applicationName, String instanceId);

    /**
     * Deregisters an application instance from Eureka.
     *
     * @param applicationName the name of the application
     * @param instanceId the unique identifier of the application instance
     * @return a Future representing the result of the deregistration operation
     */
    Future<Void> deregister(String applicationName, String instanceId);
}