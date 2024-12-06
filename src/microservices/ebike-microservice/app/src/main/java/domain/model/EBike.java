package domain.model;

import java.io.Serializable;
import ddd.Aggregate;

public class EBike implements Aggregate<String>, Serializable {


    private final String id;
    private volatile EBikeState state;
    private volatile P2d location;
    private volatile V2d direction;
    private volatile double speed; // Units per simulation tick
    private volatile int batteryLevel; // 0..100

    public EBike(String id, P2d location, EBikeState state, int battery) {
        this.id = id;
        this.state = state;
        this.location = location;
        this.direction = new V2d(1, 0); // Default direction
        this.speed = 0; // Default speed
        this.batteryLevel = battery;
    }


    public String getId() { return id; }

    public  EBikeState getState() { return state; }
    public  void setState(EBikeState state) { this.state = state; }

    public  P2d getLocation() { return location; }
    public  void setLocation(P2d location) { this.location = location; }

    public  V2d getDirection() { return direction; }
    public  void setDirection(V2d direction) { this.direction = direction; }

    public  double getSpeed() { return speed; }
    public  void setSpeed(double speed) { this.speed = speed; }

    public  int getBatteryLevel() { return batteryLevel; }
    public  void decreaseBattery(int amount) {
        this.batteryLevel = Math.max(this.batteryLevel - amount, 0);
        if (this.batteryLevel == 0) {
            this.state = EBikeState.MAINTENANCE;
        }
    }

    public  void rechargeBattery() {
        this.batteryLevel = 100;
        this.state = EBikeState.AVAILABLE;
    }

    public  boolean isAvailable() {
        return this.state == EBikeState.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("EBike{id='%s', location=%s, batteryLevel=%d%%, state='%s'}",
                id, location, batteryLevel, state);
    }

}