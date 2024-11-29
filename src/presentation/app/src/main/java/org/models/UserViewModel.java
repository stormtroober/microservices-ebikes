package org.models;



public record UserViewModel(String id, int credit, boolean admin) {
    public UserViewModel updateCredit(int credit) {
        return new UserViewModel(id, credit, admin);
    }
}