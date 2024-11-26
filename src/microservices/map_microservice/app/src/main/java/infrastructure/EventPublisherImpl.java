package infrastructure;

import application.ports.EventPublisher;
import domain.model.EBike;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class EventPublisherImpl implements EventPublisher {
    private final Vertx vertx;

    public EventPublisherImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void publishBikeUpdate(EBike bike) {
        String bikeJson = convertBikeToJson(bike);
        vertx.eventBus().publish("bikes.update", bikeJson);
    }

    @Override
    public void publishBikeUserUpdate(String username, EBike bike) {
        String bikeJson = convertBikeToJson(bike);
        vertx.eventBus().publish(username, bikeJson);
    }

    @Override
    public void publishBikeUserUpdate(EBike bike) {
        String bikeJson = convertBikeToJson(bike);
        vertx.eventBus().publish("available_bikes", bikeJson);
    }

    private String convertBikeToJson(EBike bike) {
        JsonObject json = new JsonObject();
        json.put("bikeName", bike.getBikeName());
        json.put("position", new JsonObject()
                .put("x", bike.getPosition().x())
                .put("y", bike.getPosition().y()));
        json.put("state", bike.getState().toString());
        json.put("batteryLevel", bike.getBatteryLevel()); // Add this line
        return json.encode();
    }
}