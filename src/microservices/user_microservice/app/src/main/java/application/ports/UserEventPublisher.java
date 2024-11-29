package application.ports;

import domain.model.User;
import io.vertx.core.json.JsonObject;

public interface UserEventPublisher {

    void publishUserUpdate(JsonObject user);

    void publishAllUsersUpdates(JsonObject users);

}
