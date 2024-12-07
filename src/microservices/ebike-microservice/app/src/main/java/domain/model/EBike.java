package domain.model;

import java.io.Serializable;
import ddd.Aggregate;

public class EBike implements Aggregate<String>, Serializable {


    private final String id;
    private volatile EBikeState state;
    private volatile P2d location;
    private volatile double speed; // Units per simulation tick
    private volatile int batteryLevel; // 0..100

    public EBike(String id, P2d location, EBikeState state, int battery) {
        this.id = id;
        this.state = state;
        this.location = location;
        this.speed = 0; // Default speed
        this.batteryLevel = battery;
    }

    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format("EBike{id='%s', location=%s, batteryLevel=%d%%, state='%s'}",
                id, location, batteryLevel, state);
    }

}