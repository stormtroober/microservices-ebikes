package infrastructure;

import domain.model.EBike;
import io.vertx.core.Vertx;

public class EventPublisherImpl implements EventPublisher{
    private final Vertx vertx;

    public EventPublisherImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    public void publishBikeUpdate(EBike bike) {
        vertx.eventBus().publish("bikes.update", bike.toString());
    }

    public void publishUserUpdate(String username, String event) {
        vertx.eventBus().publish("user." + username + ".update", event);
    }

    public void startBikeObservables() {
        vertx.eventBus().consumer("bikes.update", message -> {
            // WebSocket logic for broadcasting bike updates
            System.out.println("Broadcasting: " + message.body());
        });
    }

    @Override
    public void startUserBikeObservables(String username) {
        vertx.eventBus().consumer("user." + username + ".update", message -> {
            // WebSocket logic for broadcasting user bike updates
            System.out.println("User " + username + ": " + message.body());
        });
    }
}
