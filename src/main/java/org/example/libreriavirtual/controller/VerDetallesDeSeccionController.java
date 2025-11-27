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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

public class VerDetallesDeSeccionController implements Initializable {

    @FXML
    private Label lblGrado;

    @FXML
    private Label lblSeccion;

    @FXML
    private ListView<String> lstAlumnos;

    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    // Inicializador de Gson
    private final Gson gson = new Gson();

    // lista paralela de ids de alumnos en el mismo orden que lstAlumnos.items
    private final List<Integer> studentIds = new ArrayList<>();

    @FXML
    void cambiarAlPanelAula(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_AULA_FXML);
    }

    @FXML
    void cambiarAlPanelAgregarLibros(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_AGREGAR_LIBROS_FXML);
    }

    @FXML
    void eliminarAlumno(ActionEvent event) {
        int idx = lstAlumnos.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            MostrarAlerta.error("Validación", "Seleccione un alumno para eliminar.");
            return;
        }

        Integer id = (idx < studentIds.size()) ? studentIds.get(idx) : null;
        if (id == null || id <= 0) {
            MostrarAlerta.error("Error", "No se puede determinar el id del alumno seleccionado.");
            return;
        }

        // ejecutar DELETE en background
        CompletableFuture.runAsync(() -> {
            Response resp = null;
            try {
                resp = ApiClient.request("/users/" + id, "DELETE", null);
                try {
                    if (resp.isSuccessful()) {
                        // idx e id no se modifican, son efectivamente final y pueden usarse en la lambda
                        Platform.runLater(() -> {
                            // remover de la lista y de los ids
                            if (idx < lstAlumnos.getItems().size()) {
                                lstAlumnos.getItems().remove(idx);
                            }
                            if (idx < studentIds.size()) {
                                studentIds.remove(idx);
                            }
                            System.out.println("Alumno eliminado: id " + id);
                            MostrarAlerta.info("Éxito", "Alumno eliminado correctamente.");
                        });
                    } else {
                        // extraer datos necesarios antes de la lambda para no capturar 'resp'
                        String body = resp.body() != null ? resp.body().string() : "";
                        int code = resp.code();
                        System.err.println("Error al eliminar usuario id " + id + ": HTTP " + code + " - " + body);
                        Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al eliminar alumno: HTTP " + code));
                    }
                } finally {
                    //cerrar respuesta
                    if (resp != null) resp.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al eliminar alumno: " + e.getMessage()));
                if (resp != null) try { resp.close(); } catch (Exception ignored) {}
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String gradeName = SelectionContext.getGradeName();
        String sectionName = SelectionContext.getSectionName();

        lblGrado.setText(gradeName != null ? gradeName : "Desconocido");
        lblSeccion.setText(sectionName != null ? sectionName : "Desconocido");

        // cargar alumnos en background
        CompletableFuture.runAsync(() -> {
            List<String> nombres = new ArrayList<>();
            studentIds.clear();
            try {
                if (gradeName == null || sectionName == null) {
                    System.err.println("No hay contexto de selección para grado/sección.");
                } else {

                    // 1) buscar gradeId por nombre
                    Integer gradeId = null;
                    Response respGrades = ApiClient.request("/grades", "GET", null);
                    try {
                        if (respGrades.isSuccessful()) {
                            String gradesBody = respGrades.body().string();
                            JsonArray gradesArray = gson.fromJson(gradesBody, JsonArray.class);
                            if (gradesArray != null) {
                                for (JsonElement gEl : gradesArray) {
                                    if (!gEl.isJsonObject()) continue;
                                    JsonObject gObj = gEl.getAsJsonObject();
                                    String name = extractNameFrom(gObj);
                                    if (gradeName.equals(name)) {
                                        if (gObj.has("id") && !gObj.get("id").isJsonNull()) {
                                            gradeId = gObj.get("id").getAsInt();
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            System.err.println("Error al obtener grades: " + respGrades.code());
                        }
                    } finally {
                        respGrades.close();
                    }

                    // 2) buscar sectionId por nombre dentro del grade
                    Integer sectionId = null;
                    if (gradeId != null) {
                        Response respSections = ApiClient.request("/grades/" + gradeId + "/sections", "GET", null);
                        try {
                            if (respSections.isSuccessful()) {
                                String sectionsBody = respSections.body().string();
                                JsonArray sectionsArray = gson.fromJson(sectionsBody, JsonArray.class);
                                if (sectionsArray != null) {
                                    for (JsonElement sEl : sectionsArray) {
                                        if (!sEl.isJsonObject()) continue;
                                        JsonObject sObj = sEl.getAsJsonObject();
                                        String sName = extractNameFrom(sObj);
                                        if (sectionName.equals(sName)) {
                                            if (sObj.has("id") && !sObj.get("id").isJsonNull()) {
                                                sectionId = sObj.get("id").getAsInt();
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.err.println("Error al obtener secciones para grado " + gradeId + ": " + respSections.code());
                            }
                        } finally {
                            respSections.close();
                        }
                    }

                    // 3) obtener lista de alumnos usando sectionId
                    if (sectionId != null) {
                        Response respStudents = null;
                        try {
                            respStudents = ApiClient.request("/grades/" + gradeId + "/sections/" + sectionId + "/students", "GET", null);
                            if (!respStudents.isSuccessful()) {
                                respStudents.close();
                                respStudents = ApiClient.request("/sections/" + sectionId + "/students", "GET", null);
                            }

                            if (respStudents != null && respStudents.isSuccessful()) {
                                String studentsBody = respStudents.body().string();
                                JsonArray studentsArray = gson.fromJson(studentsBody, JsonArray.class);
                                if (studentsArray != null) {
                                    for (JsonElement stEl : studentsArray) {
                                        if (!stEl.isJsonObject()) continue;
                                        JsonObject stObj = stEl.getAsJsonObject();
                                        String name = extractStudentNameFrom(stObj);
                                        Integer id = extractStudentIdFrom(stObj);
                                        nombres.add(name);
                                        studentIds.add(id != null ? id : -1);
                                    }
                                }
                            } else {
                                System.err.println("No se pudieron obtener alumnos para la sección " + sectionId);
                            }
                        } finally {
                            if (respStudents != null) respStudents.close();
                        }
                    } else {
                        System.err.println("No se encontró el id de la sección seleccionada.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> lstAlumnos.setItems(FXCollections.observableArrayList(nombres)));
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

    private String extractStudentNameFrom(JsonObject obj) {
        if (obj == null) return "Alumno desconocido";
        if (obj.has("full_name") && !obj.get("full_name").isJsonNull()) return obj.get("full_name").getAsString();
        if (obj.has("fullName") && !obj.get("fullName").isJsonNull()) return obj.get("fullName").getAsString();
        if (obj.has("name") && !obj.get("name").isJsonNull()) return obj.get("name").getAsString();
        if (obj.has("nombre") && !obj.get("nombre").isJsonNull()) return obj.get("nombre").getAsString();
        String first = obj.has("first_name") && !obj.get("first_name").isJsonNull() ? obj.get("first_name").getAsString() : null;
        String last = obj.has("last_name") && !obj.get("last_name").isJsonNull() ? obj.get("last_name").getAsString() : null;
        if (first != null || last != null) {
            return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        }
        return "Alumno desconocido";
    }

    private Integer extractStudentIdFrom(JsonObject obj) {
        if (obj == null) return null;
        try {
            if (obj.has("id") && !obj.get("id").isJsonNull()) return obj.get("id").getAsInt();
            if (obj.has("user_id") && !obj.get("user_id").isJsonNull()) return obj.get("user_id").getAsInt();
            if (obj.has("student_id") && !obj.get("student_id").isJsonNull()) return obj.get("student_id").getAsInt();
        } catch (Exception e) {
            // ignorar parseo si hay problemas
        }
        return null;
    }
}