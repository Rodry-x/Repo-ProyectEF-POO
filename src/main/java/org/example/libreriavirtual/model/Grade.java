package org.example.libreriavirtual.model;

public class Grade {
    private int id;
    private String name;

    public Grade(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
