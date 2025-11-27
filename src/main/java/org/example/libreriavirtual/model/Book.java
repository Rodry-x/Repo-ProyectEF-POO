package org.example.libreriavirtual.model;

import javax.annotation.processing.Generated;

public class Book {
    private String title;
    private String author;
    private String description;
    private String file_url;
    private String category;
    private int id;
    private int course_id;
    private int created_by;
    private String created_at;

    // Constructor vacío para Gson
    public Book() {
    }

    public Book(String title, String author, String description, String file_url, String category, int id, int course_id, int created_by, String created_at) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.file_url = file_url;
        this.category = category;
        this.id = id;
        this.course_id = course_id;
        this.created_by = created_by;
        this.created_at = created_at;
    }

    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getFile_url() { return file_url; }
    public String getCategory() { return category; }
    public int getId() { return id; }
    public int getCourse_id() { return course_id; }
    public int getCreated_by() { return created_by; }
    public String getCreated_at() { return created_at; }

    // Setters (útiles para Gson)
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setDescription(String description) { this.description = description; }
    public void setFile_url(String file_url) { this.file_url = file_url; }
    public void setCategory(String category) { this.category = category; }
    public void setId(int id) { this.id = id; }
    public void setCourse_id(int course_id) { this.course_id = course_id; }
    public void setCreated_by(int created_by) { this.created_by = created_by; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}