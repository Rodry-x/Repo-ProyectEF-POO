package org.example.libreriavirtual.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.libreriavirtual.utilities.ApiUtils;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

import java.io.IOException;

public class EstanteriaVirtualController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    void cambiarALibroCienciaYAmbiente(MouseEvent event) {
        //Cambiando de escena y poniendo el nombre del libro
        sceneController.cambiarACrudLibros(event, "Ciencia y Ambiente");
    }

    @FXML
    void cambiarALibroCienciasSociales(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Ciencias Sociales");
    }

    @FXML
    void cambiarALibroComunicacion(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Comunicación");
    }

    @FXML
    void cambiarALibroHistoria(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Historia");
    }

    @FXML
    void cambiarALibroMatematicas(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Matemáticas");
    }

    @FXML
    void cambiarALibroRazMatematico(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Razonamiento Matemático");
    }

    @FXML
    void cambiarALibroRazVerbal(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Razonamiento Verbal");
    }

    @FXML
    void cambiarALibroReligion(MouseEvent event) {
        sceneController.cambiarACrudLibros(event, "Religión");
    }

    @FXML
    void cambiarAPanelPrincipal(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }

    private void openCrudByCourseName(javafx.event.Event fxEvent, String courseName) {
        Thread th = new Thread(() -> {
            Integer courseId = null;
            try {
                JsonArray courses = ApiUtils.getJsonArray("/courses");
                if (courses != null) {
                    for (JsonElement ce : courses) {
                        if (!ce.isJsonObject()) continue;
                        JsonObject c = ce.getAsJsonObject();
                        try {
                            String name = null;
                            if (c.has("name") && c.get("name").isJsonPrimitive()) name = c.get("name").getAsString();
                            if (name == null && c.has("title") && c.get("title").isJsonPrimitive()) name = c.get("title").getAsString();
                            if (name != null && name.equalsIgnoreCase(courseName)) {
                                if (c.has("id") && c.get("id").isJsonPrimitive()) courseId = c.get("id").getAsInt();
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (IOException e) {
                System.err.println("[Estanteria] Error al obtener courses: " + e.getMessage());
            }

            final Integer cid = courseId;
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(Path.PANEL_LIBROS_FXML));
                    Parent root = loader.load();
                    CrudLibrosController ctrl = loader.getController();
                    if (cid != null && cid > 0) ctrl.initData(cid, courseName);
                    else ctrl.initData(-1, courseName + " (id no encontrado)");
                    Stage stage = (Stage) ((Node) fxEvent.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (Exception e) {
                    System.err.println("[Estanteria] Error al abrir CrudLibros: " + e.getMessage());
                }
            });
        });
        th.setDaemon(true);
        th.start();
    }
}
