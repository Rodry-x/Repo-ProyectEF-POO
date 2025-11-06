package org.example.libreriavirtual.model;

public class Section {
    private int id;
    private int grade_id;
    private String name;

    public Section(int id, int grade_id, String name) {
        this.id = id;
        this.grade_id = grade_id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getGrade_id() {
        return grade_id;
    }

    public String getName() {
        return name;
    }
}
