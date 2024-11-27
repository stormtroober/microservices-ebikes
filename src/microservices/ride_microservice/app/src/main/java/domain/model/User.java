package domain.model;

public class User {
    public enum UserType { ADMIN, USER }

    private final String id;
    private final UserType type;
    private volatile int credit;

    public User(String id, UserType type, int credit) {
        this.id = id;
        this.type = type;
        this.credit = credit; // Default credit
    }

    public String getId() { return id; }
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
        return String.format("User{id='%s', type='%s', credit=%d}", id, type, credit);
    }
}