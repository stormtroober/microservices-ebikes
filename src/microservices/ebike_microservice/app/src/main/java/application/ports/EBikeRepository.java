package application.ports;

import ddd.Repository;
import domain.model.EBike;

import java.util.List;
import java.util.Optional;

public interface EBikeRepository extends Repository {
    void save(EBike ebike);
    void update(EBike ebike);
    Optional<EBike> findById(String id);
    List<EBike> findAll();
}
