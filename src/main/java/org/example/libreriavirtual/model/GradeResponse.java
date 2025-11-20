package org.example.libreriavirtual.model;

public class GradeResponse {
    private Integer id;
    private String name;

    public GradeResponse() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GradeResponse{id=" + id + ", name='" + name + "'}";
    }
}
