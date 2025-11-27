package org.example.libreriavirtual.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import okhttp3.Response;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.SelectionContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CrudAulaController implements Initializable {
    private final SceneController sceneController = new SceneController();

    @FXML
    private ListView<String> lstSecciones;

    private final Gson gson = new Gson();

    @FXML
    void agregarSeccion(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_SECCIONES_FXML);
    }

    @FXML
    void cambiarAlPanelProfesor(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
    }

    @FXML
    void verDetallesDeSecciones(ActionEvent event) {
        String selected = lstSecciones.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            MostrarAlerta.error("Error", "No se ha seleccionado ninguna sección.");
            return;
        }
        // formato esperado: "GradoName - SeccionName"
        String[] parts = selected.split(" - ", 2);
        String gradeName = parts.length > 0 ? parts[0].trim() : "";
        String sectionName = parts.length > 1 ? parts[1].trim() : "";

        if (gradeName.isEmpty() || sectionName.isEmpty()) {
            System.err.println("Elemento seleccionado con formato inesperado: " + selected);
            MostrarAlerta.advertencia("Advertencia", "El elemento seleccionado tiene un formato inesperado.");
            return;
        }

        SelectionContext.setGradeName(gradeName);
        SelectionContext.setSectionName(sectionName);

        // cambiar a la vista de detalles
        sceneController.cambiarEscena(event, Path.PANEL_VER_DETALLES_SECCION_FXML);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarGradosYSecciones();
    }

    private void cargarGradosYSecciones() {
        CompletableFuture.runAsync(() -> {
            List<String> items = new ArrayList<>();
            try {
                Response respGrades = ApiClient.request("/grades", "GET", null);
                try {
                    if (!respGrades.isSuccessful()) {
                        System.err.println("Error al obtener grades: " + respGrades.code());
                        return;
                    }
                    String gradesBody = respGrades.body().string();
                    JsonArray gradesArray = gson.fromJson(gradesBody, JsonArray.class);
                    if (gradesArray == null) return;

                    for (JsonElement gradeEl : gradesArray) {
                        if (!gradeEl.isJsonObject()) continue;
                        JsonObject gradeObj = gradeEl.getAsJsonObject();
                        int gradeId = gradeObj.has("id") && !gradeObj.get("id").isJsonNull()
                                ? gradeObj.get("id").getAsInt() : -1;
                        String gradeName = extractNameFrom(gradeObj);
                        if (gradeId == -1) continue;

                        Response respCourses = ApiClient.request("/grades/" + gradeId + "/sections", "GET", null);
                        try {
                            if (!respCourses.isSuccessful()) {
                                System.err.println("Error al obtener courses para grado " + gradeId + ": " + respCourses.code());
                                continue;
                            }
                            String coursesBody = respCourses.body().string();
                            JsonArray coursesArray = gson.fromJson(coursesBody, JsonArray.class);
                            if (coursesArray == null) continue;

                            for (JsonElement courseEl : coursesArray) {
                                if (!courseEl.isJsonObject()) continue;
                                JsonObject courseObj = courseEl.getAsJsonObject();
                                String courseName = extractNameFrom(courseObj);
                                items.add(gradeName + " - " + courseName);
                            }
                        } finally {
                            respCourses.close();
                        }
                    }
                } finally {
                    respGrades.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> lstSecciones.setItems(FXCollections.observableArrayList(items)));
        });
    }

    private String extractNameFrom(JsonObject obj) {
        if (obj == null) return "Desconocido";
        if (obj.has("name") && !obj.get("name").isJsonNull()) return obj.get("name").getAsString();
        if (obj.has("nombre") && !obj.get("nombre").isJsonNull()) return obj.get("nombre").getAsString();
        if (obj.has("grade") && !obj.get("grade").isJsonNull()) return obj.get("grade").getAsString();
        if (obj.has("section") && !obj.get("section").isJsonNull()) return obj.get("section").getAsString();
        return "Desconocido";
    }
}