package org.example.libreriavirtual.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.libreriavirtual.utilities.ApiUtils;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrudLibrosController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    private Label lblNombreDelLibro;

    @FXML
    private ListView<String> lstLibros;

    // estado actual del curso para poder refrescar
    private volatile int currentCourseId = -1;
    private volatile String currentCourseName = null;

    public void setNombreDelLibro(String nombre) {
        lblNombreDelLibro.setText(nombre);
    }

    @FXML
    void cambiarAEstanteria(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTANTERIA_FXML);
    }

    @FXML
    void ActualizarListaDeLibros(ActionEvent event) {
        // recargar usando el courseId actual; si no existe intentar resolver por nombre
        if (currentCourseId > 0) {
            cargarLibrosPorCurso(currentCourseId);
            return;
        }

        if (currentCourseName == null || currentCourseName.isBlank()) {
            Platform.runLater(() -> {
                lstLibros.getItems().clear();
                lstLibros.getItems().add("No hay course_id válido para refrescar la lista");
            });
            return;
        }

        // resolver courseId por nombre en background
        Thread resolver = new Thread(() -> {
            try {
                Integer resolvedId = buscarCourseIdPorNombre(currentCourseName);
                if (resolvedId != null && resolvedId > 0) {
                    currentCourseId = resolvedId;
                    cargarLibrosPorCurso(currentCourseId);
                } else {
                    Platform.runLater(() -> {
                        lstLibros.getItems().clear();
                        lstLibros.getItems().add("No se encontró course_id para: " + currentCourseName);
                    });
                }
            } catch (Exception e) {
                System.err.println("[CrudLibros] Error resolviendo course_id: " + e.getMessage());
                Platform.runLater(() -> {
                    lstLibros.getItems().clear();
                    lstLibros.getItems().add("Error al intentar resolver course_id");
                });
            }
        });
        resolver.setDaemon(true);
        resolver.start();
    }

    // llamado por el cargador de la escena para inicializar con el courseId
    public void initData(int courseId, String courseName) {
        this.currentCourseId = courseId;
        this.currentCourseName = courseName;
        lblNombreDelLibro.setText(courseName != null ? courseName : "Curso");
        if (courseId > 0) cargarLibrosPorCurso(courseId);
        else {
            // si no hay id válido, limpiar o mostrar mensaje
            Platform.runLater(() -> {
                lstLibros.getItems().clear();
                lstLibros.getItems().add("No se encontró course_id para este curso");
            });
        }
    }

    private void cargarLibrosPorCurso(int courseId) {
        Platform.runLater(() -> lstLibros.getItems().clear());
        Thread th = new Thread(() -> {
            try {
                JsonArray arr = ApiUtils.getJsonArray("/courses/" + courseId + "/books");
                List<String> items = new ArrayList<>();
                if (arr != null) {
                    for (JsonElement el : arr) {
                        if (!el.isJsonObject()) continue;
                        JsonObject o = el.getAsJsonObject();
                        String detalle = formatoLibro(o);
                        items.add(detalle);
                    }
                }
                final List<String> finalItems = items;
                Platform.runLater(() -> {
                    lstLibros.getItems().setAll(finalItems);
                    if (finalItems.isEmpty()) lstLibros.getItems().add("No hay libros para este curso");
                });
            } catch (IOException e) {
                System.err.println("[CrudLibros] Error al obtener libros: " + e.getMessage());
                Platform.runLater(() -> {
                    lstLibros.getItems().clear();
                    lstLibros.getItems().add("Error al cargar libros");
                });
            }
        });
        th.setDaemon(true);
        th.start();
    }

    // Construye una línea descriptiva con todos los campos relevantes encontrados
    private String formatoLibro(JsonObject o) {
        StringBuilder sb = new StringBuilder();
        String id = extraerCampo(o, "id");
        if (id != null) sb.append("id: ").append(id).append(" ; ");

        String title = extraerCampo(o, "title", "name", "nombre");
        if (title != null) sb.append("titulo: ").append(title).append(" ; ");

        String author = extraerCampo(o, "author", "autor", "authors");
        if (author != null) sb.append("autor: ").append(author).append(" ; ");

        String isbn = extraerCampo(o, "isbn", "isbn13");
        if (isbn != null) sb.append("isbn: ").append(isbn).append(" ; ");

        String publisher = extraerCampo(o, "publisher", "editorial");
        if (publisher != null) sb.append("editorial: ").append(publisher).append(" ; ");

        String year = extraerCampo(o, "year", "año", "published_year", "published_at");
        if (year != null) sb.append("año: ").append(year).append(" ; ");

        String pages = extraerCampo(o, "pages", "paginas");
        if (pages != null) sb.append("páginas: ").append(pages).append(" ; ");

        String language = extraerCampo(o, "language", "idioma");
        if (language != null) sb.append("idioma: ").append(language).append(" ; ");

        String description = extraerCampo(o, "description", "resumen", "abstract");
        if (description != null) sb.append("descripcion: ").append(trunc(description, 200)).append(" ; ");

        // Si no se encontró nada, mostrar el objeto completo como fallback
        if (sb.length() == 0) {
            return o.toString();
        }

        // quitar el último " ; "
        if (sb.length() >= 3) sb.setLength(sb.length() - 3);
        return sb.toString();
    }

    // intenta obtener el primer campo no vacío entre los indicados
    private String extraerCampo(JsonObject o, String... keys) {
        try {
            for (String k : keys) {
                if (o.has(k) && o.get(k).isJsonPrimitive()) {
                    String v = o.get(k).getAsString();
                    if (v != null && !v.isBlank()) return v;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String trunc(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max).trim() + "...";
    }

    // Busca el course_id consultando /courses por nombre (coincidencia case-insensitive)
    private Integer buscarCourseIdPorNombre(String courseName) throws IOException {
        if (courseName == null || courseName.isBlank()) return null;
        JsonArray courses = ApiUtils.getJsonArray("/courses");
        if (courses == null) return null;
        for (JsonElement ce : courses) {
            if (!ce.isJsonObject()) continue;
            JsonObject c = ce.getAsJsonObject();
            String name = null;
            if (c.has("name") && c.get("name").isJsonPrimitive()) name = c.get("name").getAsString();
            if ((name == null || name.isBlank()) && c.has("title") && c.get("title").isJsonPrimitive()) name = c.get("title").getAsString();
            if (name != null && name.equalsIgnoreCase(courseName)) {
                if (c.has("id") && c.get("id").isJsonPrimitive()) return c.get("id").getAsInt();
            }
        }
        return null;
    }
}