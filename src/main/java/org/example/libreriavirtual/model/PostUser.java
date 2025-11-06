package org.example.libreriavirtual.model;

public class PostUser {
    private String full_name;
    private String email;
    private String password;
    private String role;

    public PostUser(String full_name, String email, String password, String role) {
        this.full_name = full_name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
