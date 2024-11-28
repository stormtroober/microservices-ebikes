package domain.model;

import java.io.Serializable;
import ddd.Aggregate;

public class User implements Aggregate<String>, Serializable {
    public enum UserType { ADMIN, USER }

    private final String username;
    private final UserType type;
    private volatile int credit;

    public User(String username, UserType type, int credit) {
        this.username = username;
        this.type = type;
        this.credit = credit; // Default credit
    }

    public String getUsername() { return username; }
    public UserType getType() { return type; }

    public int getCredit() { return credit; }

    public void decreaseCredit(int amount) {
        this.credit = Math.max(this.credit - amount, 0);
    }

    public void increaseCredit(int amount) {
        this.credit += amount;
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', type='%s', credit=%d}", username, type, credit);
    }
}
