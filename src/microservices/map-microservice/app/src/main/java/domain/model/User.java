package domain.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {

    private final String username;
    private final Set<EBike> userBikes;

    public User(String username) {
        this.username = username;
        this.userBikes = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public Set<EBike> getUserBikes() {
        return userBikes;
    }

    public void addBike(EBike bike) {
        userBikes.add(bike);
    }

    public void removeBike(EBike bike) {
        userBikes.remove(bike);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", userBikes=" + userBikes +
                '}';
    }
}
