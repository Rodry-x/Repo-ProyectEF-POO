module org.example.libreriavirtual {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.graphics;
    requires java.sql;
    requires com.google.gson;
    requires okhttp3;
    requires javafx.base;
    requires java.compiler;

    exports org.example.libreriavirtual.application;
    opens org.example.libreriavirtual.application to javafx.fxml;

    exports org.example.libreriavirtual.controller;
    opens org.example.libreriavirtual.controller to javafx.fxml;

    exports org.example.libreriavirtual.model;
    opens org.example.libreriavirtual.model to com.google.gson;

}