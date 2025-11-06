package org.example.libreriavirtual.model;

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

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getFile_url() {
        return file_url;
    }

    public String getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public int getCreated_by() {
        return created_by;
    }

    public String getCreated_at() {
        return created_at;
    }
}
