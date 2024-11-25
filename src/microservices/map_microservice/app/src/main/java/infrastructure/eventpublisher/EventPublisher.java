package infrastructure.eventpublisher;

import domain.model.EBike;

public interface EventPublisher {
    void publishBikeUpdate(EBike bike);
    void publishBikeUserUpdate(String username, EBike bike);
    void publishBikeUserUpdate(EBike bike);
}
