package domain.model;

import ddd.ValueObject;

import java.io.Serializable;

public class EBike implements ValueObject, Serializable {

    private final String bikeName;
    private P2d position;

    public EBike(String bikeName, P2d position) {
        this.bikeName = bikeName;
        this.position = position;
    }

    public String getBikeName() {
        return bikeName;
    }

    public P2d getPosition() {
        return position;
    }

    public void setPosition(P2d position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "bikeName='" + bikeName + '\'' +
                ", position=" + position +
                '}';
    }
}
