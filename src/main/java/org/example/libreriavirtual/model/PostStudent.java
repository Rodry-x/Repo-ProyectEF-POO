package org.example.libreriavirtual.model;

public class PostStudent {
    private String full_name;
    private String email;
    private String role;
    private String password;

    public PostStudent() {}

    public PostStudent(String full_name, String email, String role, String password) {
        this.full_name = full_name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
