package org.example.libreriavirtual.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String full_name;
    private String email;
    private String role;
    private String created_at;

    public User(int id, String full_name, String email, String role, String created_at) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.role = role;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getCreated_at() {
        return created_at;
    }
}
