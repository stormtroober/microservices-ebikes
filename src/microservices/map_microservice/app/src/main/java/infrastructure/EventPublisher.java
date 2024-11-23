package infrastructure;

import domain.model.EBike;

public interface EventPublisher {
    void publishBikeUpdate(EBike bike);
    void startBikeObservables();
    void startUserBikeObservables(String username);
}
