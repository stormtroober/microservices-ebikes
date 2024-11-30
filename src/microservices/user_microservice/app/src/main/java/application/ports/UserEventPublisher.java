package application.ports;

import domain.model.User;
import io.vertx.core.json.JsonObject;

public interface UserEventPublisher {

    void publishUserUpdate(String username, JsonObject user);

    void publishAllUsersUpdates(JsonObject users);

}
