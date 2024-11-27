package application.ports;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MapCommunicationPort {
    void sendUpdate(JsonObject ebike);
    void sendAllUpdates(JsonArray ebikes);
}
