package infrastructure;

import application.ports.UserEventPublisher;
import domain.model.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class UserEventPublisherImpl implements  UserEventPublisher{
    private final Vertx vertx;

    public UserEventPublisherImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void publishUserUpdate(JsonObject user) {
        vertx.eventBus().publish("user.update." + user.getString("username"), user);
    }

    @Override
    public void publishAllUsersUpdates(JsonObject users) {
        vertx.eventBus().publish("users.update", users);
    }
}
