package application.ports;

import domain.model.EBike;

import java.util.List;

public interface EventPublisher {
    void publishBikesUpdate(List<EBike> bikes);
    void publishUserBikesUpdate(List<EBike> bikes, String username);
    void publishUserAvailableBikesUpdate(List<EBike> bikes);
    void publishStopRide(String username);
}
