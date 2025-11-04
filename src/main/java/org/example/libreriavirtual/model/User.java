package org.example.libreriavirtual.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String full_name;
    private String email;
    private String password_hash;
    private String role;
    private LocalDateTime created_at;

    public User(int id, String full_name, String email, String password_hash, String role, LocalDateTime created_at) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.created_at = created_at;
    }
}
