package org.example.libreriavirtual.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import okhttp3.Response;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.SelectionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CrudAgregarLibrosController {

    @FXML
    private ComboBox<String> cmbCursos;

    @FXML
    private TextField txtAutor;

    @FXML
    private TextField txtCategoria;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtNombreCurso;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtUrlDelPDF;

    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    // mapa nombreCurso -> courseId
    private final Map<String, Integer> cursosMap = new HashMap<>();

    //inicializar gson
    private final Gson gson = new Gson();

    @FXML
    void cambiarAlPanelVerDetallesSeccion(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_VER_DETALLES_SECCION_FXML);
    }

    @FXML
    private void initialize() {
        // carga los cursos al iniciar el controlador
        CompletableFuture.runAsync(() -> {
            try {
                Integer sectionId = null;
                Integer gradeId = null;

                // 1) intentar obtener sectionId / gradeId directamente desde SelectionContext
                try {
                    Object sid = SelectionContext.class.getMethod("getSectionId").invoke(null);
                    if (sid instanceof Number) sectionId = ((Number) sid).intValue();
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }

                try {
                    Object gid = SelectionContext.class.getMethod("getGradeId").invoke(null);
                    if (gid instanceof Number) gradeId = ((Number) gid).intValue();
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }

                if (sectionId != null) {
                    cargarCursosParaSeccion(gradeId, sectionId);
                    return;
                }

                // 2) fallback: resolver por nombres (gradeName + sectionName)
                String gradeName = null;
                String sectionName = null;
                try {
                    Object gn = SelectionContext.class.getMethod("getGradeName").invoke(null);
                    if (gn instanceof String) gradeName = ((String) gn).trim();
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }
                try {
                    Object sn = SelectionContext.class.getMethod("getSectionName").invoke(null);
                    if (sn instanceof String) sectionName = ((String) sn).trim();
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }

                if (gradeName == null || sectionName == null) {
                    // no hay contexto suficiente; no cargar nada
                    return;
                }

                // resolver gradeId
                Response respGrades = null;
                try {
                    respGrades = ApiClient.request("/grades", "GET", null);
                    if (respGrades != null && respGrades.isSuccessful()) {
                        String body = respGrades.body() != null ? respGrades.body().string() : "";
                        JsonArray arr = gson.fromJson(body, JsonArray.class);
                        if (arr != null) {
                            for (JsonElement el : arr) {
                                if (!el.isJsonObject()) continue;
                                JsonObject obj = el.getAsJsonObject();
                                String name = extractNameFrom(obj);
                                if (gradeName.equals(name) && obj.has("id") && !obj.get("id").isJsonNull()) {
                                    gradeId = obj.get("id").getAsInt();
                                    break;
                                }
                            }
                        }
                    }
                } finally {
                    if (respGrades != null) respGrades.close();
                }

                if (gradeId == null) return;

                // resolver sectionId dentro del grade encontrado
                Response respSections = null;
                try {
                    respSections = ApiClient.request("/grades/" + gradeId + "/sections", "GET", null);
                    if (respSections != null && respSections.isSuccessful()) {
                        String body = respSections.body() != null ? respSections.body().string() : "";
                        JsonArray arr = gson.fromJson(body, JsonArray.class);
                        if (arr != null) {
                            for (JsonElement el : arr) {
                                if (!el.isJsonObject()) continue;
                                JsonObject obj = el.getAsJsonObject();
                                String name = extractNameFrom(obj);
                                if (sectionName.equals(name) && obj.has("id") && !obj.get("id").isJsonNull()) {
                                    sectionId = obj.get("id").getAsInt();
                                    break;
                                }
                            }
                        }
                    }
                } finally {
                    if (respSections != null) respSections.close();
                }

                if (sectionId == null) return;

                // finalmente cargar cursos para la sección resuelta
                cargarCursosParaSeccion(gradeId, sectionId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void guardarDatosDelLibro(ActionEvent event) {
        String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
        String autor = txtAutor.getText() != null ? txtAutor.getText().trim() : "";
        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
        String fileUrl = txtUrlDelPDF.getText() != null ? txtUrlDelPDF.getText().trim() : "";
        String categoria = txtCategoria.getText() != null ? txtCategoria.getText().trim() : "";
        String seleccionado = cmbCursos.getSelectionModel().getSelectedItem();

        if (seleccionado == null || seleccionado.isEmpty()) {
            MostrarAlerta.error("Validación", "Seleccione un curso.");
            return;
        }
        if (titulo.isEmpty()) {
            MostrarAlerta.error("Validación", "Ingrese título del libro.");
            return;
        }
        Integer courseId = cursosMap.get(seleccionado);
        if (courseId == null) {
            MostrarAlerta.error("Error", "No se pudo resolver el curso seleccionado.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            Response resp = null;
            Response rUser = null;
            try {
                // resolver y verificar created_by (id de usuario)
                Integer createdBy = resolverYVerificarUserId();
                if (createdBy == null) {
                    Platform.runLater(() -> MostrarAlerta.error("Error", "No se pudo resolver el usuario (created_by). Inicie sesión o seleccione un usuario válido."));
                    return;
                }

                // verificar que el usuario exista en la API
                try {
                    rUser = ApiClient.request("/users/" + createdBy, "GET", null);
                    if (rUser == null || !rUser.isSuccessful()) {
                        Platform.runLater(() -> MostrarAlerta.error("Error", "User id " + createdBy + " no encontrado en la API."));
                        return;
                    }
                } finally {
                    if (rUser != null) rUser.close();
                    rUser = null;
                }

                JsonObject payload = new JsonObject();
                payload.addProperty("title", titulo);
                payload.addProperty("author", autor);
                if (!descripcion.isEmpty()) payload.addProperty("description", descripcion);
                if (!fileUrl.isEmpty()) payload.addProperty("file_url", fileUrl);
                if (!categoria.isEmpty()) payload.addProperty("category", categoria);
                payload.addProperty("created_by", createdBy);

                resp = ApiClient.request("/courses/" + courseId + "/books", "POST", payload.toString());
                try {
                    if (resp != null && resp.isSuccessful()) {
                        String respBody = resp.body() != null ? resp.body().string() : "";
                        Platform.runLater(() -> {
                            MostrarAlerta.info("Éxito", "Libro registrado correctamente.");
                            txtTitulo.clear();
                            txtAutor.clear();
                            txtDescripcion.clear();
                            txtUrlDelPDF.clear();
                            txtCategoria.clear();
                        });
                        System.out.println("Libro creado: " + respBody);
                    } else {
                        String respBody = resp != null && resp.body() != null ? resp.body().string() : "";
                        int code = resp != null ? resp.code() : -1;
                        System.err.println("Error al crear libro: HTTP " + code + " - " + respBody);
                        Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear libro: HTTP " + code));
                    }
                } finally {
                    if (resp != null) resp.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear libro: " + e.getMessage()));
                if (resp != null) try { resp.close(); } catch (Exception ignored) {}
                if (rUser != null) try { rUser.close(); } catch (Exception ignored) {}
            }
        });
    }

    @FXML
    void guardarNombreDelCurso(ActionEvent event) {
        String courseName = txtNombreCurso.getText() != null ? txtNombreCurso.getText().trim() : "";
        if (courseName.isEmpty()) {
            MostrarAlerta.error("Validación", "Ingrese el nombre del curso.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            Response resp = null;
            try {
                // intentar obtener sectionId directamente desde SelectionContext si existe
                Integer sectionId = null;
                Integer gradeId = null;
                try {
                    sectionId = (Integer) SelectionContext.class.getMethod("getSectionId").invoke(null);
                } catch (NoSuchMethodException ignored) {
                    // Si SelectionContext no tiene getSectionId, seguiremos por nombre
                } catch (Exception ignored) {
                }

                // Si no se obtuvo sectionId, resolver por nombres (grade + section) como fallback
                if (sectionId == null) {
                    String gradeName = SelectionContext.getGradeName();
                    String sectionName = SelectionContext.getSectionName();
                    if (gradeName == null || sectionName == null) {
                        Platform.runLater(() -> MostrarAlerta.error("Error", "No hay contexto de grado/sección para crear el curso."));
                        return;
                    }

                    Response respGrades = ApiClient.request("/grades", "GET", null);
                    try {
                        if (respGrades.isSuccessful()) {
                            String body = respGrades.body().string();
                            JsonArray arr = gson.fromJson(body, JsonArray.class);
                            if (arr != null) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String name = extractNameFrom(obj);
                                    if (gradeName.equals(name) && obj.has("id") && !obj.get("id").isJsonNull()) {
                                        gradeId = obj.get("id").getAsInt();
                                        break;
                                    }
                                }
                            }
                        } else {
                            System.err.println("Error al obtener grades: " + respGrades.code());
                        }
                    } finally {
                        if (respGrades != null) respGrades.close();
                    }

                    if (gradeId == null) {
                        Platform.runLater(() -> MostrarAlerta.error("Error", "No se encontró el grado seleccionado."));
                        return;
                    }

                    Response respSections = ApiClient.request("/grades/" + gradeId + "/sections", "GET", null);
                    try {
                        if (respSections.isSuccessful()) {
                            String body = respSections.body().string();
                            JsonArray arr = gson.fromJson(body, JsonArray.class);
                            if (arr != null) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String name = extractNameFrom(obj);
                                    if (sectionName.equals(name) && obj.has("id") && !obj.get("id").isJsonNull()) {
                                        sectionId = obj.get("id").getAsInt();
                                        break;
                                    }
                                }
                            }
                        } else {
                            System.err.println("Error al obtener sections: " + respSections.code());
                        }
                    } finally {
                        if (respSections != null) respSections.close();
                    }

                    if (sectionId == null) {
                        Platform.runLater(() -> MostrarAlerta.error("Error", "No se encontró la sección seleccionada."));
                        return;
                    }
                }

                // resolver y verificar teacher_id (usa el método que centraliza la lógica)
                Integer teacherId = resolverYVerificarTeacherId();
                if (teacherId == null) {
                    Platform.runLater(() -> MostrarAlerta.error("Error", "No se pudo resolver el teacher_id. Inicie sesión como profesor o seleccione uno."));
                    return;
                }

                // verificar que el teacher existe en la API
                Response rUser = null;
                try {
                    rUser = ApiClient.request("/users/" + teacherId, "GET", null);
                    if (rUser == null || !rUser.isSuccessful()) {
                        if (rUser != null) rUser.close();
                        Platform.runLater(() -> MostrarAlerta.error("Error", "Teacher id " + teacherId + " no encontrado en la API."));
                        return;
                    }
                } finally {
                    if (rUser != null) rUser.close();
                }

                // Construir payload para crear course (incluye teacher_id)
                JsonObject payload = new JsonObject();
                payload.addProperty("name", courseName);
                payload.addProperty("teacher_id", teacherId);
                if (txtDescripcion.getText() != null && !txtDescripcion.getText().trim().isEmpty()) {
                    payload.addProperty("description", txtDescripcion.getText().trim());
                }

                resp = ApiClient.request("/sections/" + sectionId + "/courses", "POST", payload.toString());
                try {
                    if (resp.isSuccessful()) {
                        String respBody = resp.body() != null ? resp.body().string() : "";
                        Platform.runLater(() -> {
                            MostrarAlerta.info("Éxito", "Curso creado correctamente.");
                            txtNombreCurso.clear();
                            txtDescripcion.clear();
                        });
                        System.out.println("Curso creado: " + respBody);

                        // actualizar cmbCursos para reflejar el nuevo curso
                        cargarCursosParaSeccion(gradeId, sectionId);

                    } else {
                        String respBody = resp.body() != null ? resp.body().string() : "";
                        int code = resp.code();
                        System.err.println("Error al crear curso: HTTP " + code + " - " + respBody);
                        Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear curso: HTTP " + code));
                    }
                } finally {
                    if (resp != null) resp.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear curso: " + e.getMessage()));
                if (resp != null) try { resp.close(); } catch (Exception ignored) {}
            }
        });
    }

    // recarga la lista de cursos para la sección indicada y actualiza el ComboBox en la UI
    private void cargarCursosParaSeccion(Integer gradeId, Integer sectionId) {
        if (sectionId == null) return;
        CompletableFuture.runAsync(() -> {
            Response resp = null;
            List<String> cursos = new ArrayList<>();
            cursosMap.clear();
            try {
                resp = ApiClient.request("/sections/" + sectionId + "/courses", "GET", null);
                if (resp == null || !resp.isSuccessful()) {
                    if (resp != null) resp.close();
                    if (gradeId != null) {
                        resp = ApiClient.request("/grades/" + gradeId + "/sections/" + sectionId + "/courses", "GET", null);
                    } else {
                        resp = null;
                    }
                }

                if (resp != null) {
                    try {
                        if (resp.isSuccessful()) {
                            String body = resp.body() != null ? resp.body().string() : "";
                            JsonArray arr = gson.fromJson(body, JsonArray.class);
                            if (arr != null) {
                                for (JsonElement el : arr) {
                                    if (!el.isJsonObject()) continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String name = extractNameFrom(obj);
                                    if (name == null) continue;

                                    Integer id = null;
                                    try {
                                        if (obj.has("id") && !obj.get("id").isJsonNull()) {
                                            id = obj.get("id").getAsInt();
                                        } else if (obj.has("course_id") && !obj.get("course_id").isJsonNull()) {
                                            id = obj.get("course_id").getAsInt();
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    cursos.add(name);
                                    if (id != null) {
                                        cursosMap.put(name, id);
                                    }
                                }
                            }
                        } else {
                            System.err.println("Error al obtener cursos: HTTP " + resp.code());
                        }
                    } finally {
                        resp.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                cmbCursos.setItems(FXCollections.observableArrayList(cursos));
                if (!cursos.isEmpty()) {
                    cmbCursos.getSelectionModel().selectFirst();
                }
            });
        });
    }

    private String extractNameFrom(JsonObject obj) {
        if (obj == null) return null;
        if (obj.has("name") && !obj.get("name").isJsonNull()) return obj.get("name").getAsString();
        if (obj.has("nombre") && !obj.get("nombre").isJsonNull()) return obj.get("nombre").getAsString();
        if (obj.has("title") && !obj.get("title").isJsonNull()) return obj.get("title").getAsString();
        if (obj.has("full_name") && !obj.get("full_name").isJsonNull()) return obj.get("full_name").getAsString();
        return null;
    }

    private Integer resolverYVerificarTeacherId() {
        Integer teacherId = null;
        // 1) intentar SelectionContext.getTeacherId()
        try {
            Object t = SelectionContext.class.getMethod("getTeacherId").invoke(null);
            if (t instanceof Number) teacherId = ((Number) t).intValue();
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ignored) {
        }

        // 2) fallback SelectionContext.getUserId()
        if (teacherId == null) {
            try {
                Object u = SelectionContext.class.getMethod("getUserId").invoke(null);
                if (u instanceof Number) teacherId = ((Number) u).intValue();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }

        // 3) fallback SesionController.getProfesorIdActivo()
        if (teacherId == null) {
            try {
                teacherId = org.example.libreriavirtual.utilities.SesionController.getProfesorIdActivo();
            } catch (Exception ignored) {
            }
        }

        // 4) fallback SesionController.getProfesorActivo()
        if (teacherId == null) {
            try {
                var prof = org.example.libreriavirtual.utilities.SesionController.getProfesorActivo();
                if (prof != null && prof.getId() > 0) teacherId = prof.getId();
            } catch (Exception ignored) {
            }
        }

        return teacherId;
    }

    private Integer resolverYVerificarUserId() {
        Integer userId = null;

        // Intenta varios getters en SelectionContext vía reflexión
        String[] scMethods = {
                "getUserId", "getUserIdActivo", "getTeacherId", "getProfesorIdActivo",
                "getUsuarioActivo", "getUserActivo"
        };
        for (String m : scMethods) {
            if (userId != null) break;
            try {
                var method = SelectionContext.class.getMethod(m);
                Object val = method.invoke(null);
                if (val instanceof Number) {
                    userId = ((Number) val).intValue();
                    System.err.println("resolverYVerificarUserId: encontrado en SelectionContext." + m + " -> " + userId);
                    break;
                }
                if (val != null) {
                    try {
                        var idMethod = val.getClass().getMethod("getId");
                        Object idVal = idMethod.invoke(val);
                        if (idVal instanceof Number) {
                            userId = ((Number) idVal).intValue();
                            System.err.println("resolverYVerificarUserId: encontrado en SelectionContext." + m + ".getId() -> " + userId);
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                // no matar la ejecución por reflexión; solo informar
                System.err.println("resolverYVerificarUserId: error invocando SelectionContext." + m + " -> " + e.getMessage());
            }
        }

        // Si no aparece, intentar en SesionController vía reflexión
        if (userId == null) {
            try {
                Class<?> sc = Class.forName("org.example.libreriavirtual.utilities.SesionController");
                String[] sesMethods = {
                        "getUserIdActivo", "getUsuarioActivo", "getUserActivo",
                        "getProfesorIdActivo", "getProfesorActivo"
                };
                for (String m : sesMethods) {
                    if (userId != null) break;
                    try {
                        var method = sc.getMethod(m);
                        Object val = method.invoke(null);
                        if (val instanceof Number) {
                            userId = ((Number) val).intValue();
                            System.err.println("resolverYVerificarUserId: encontrado en SesionController." + m + " -> " + userId);
                            break;
                        }
                        if (val != null) {
                            try {
                                var idMethod = val.getClass().getMethod("getId");
                                Object idVal = idMethod.invoke(val);
                                if (idVal instanceof Number) {
                                    userId = ((Number) idVal).intValue();
                                    System.err.println("resolverYVerificarUserId: encontrado en SesionController." + m + ".getId() -> " + userId);
                                    break;
                                }
                            } catch (NoSuchMethodException ignored) {
                            }
                        }
                    } catch (NoSuchMethodException ignored) {
                    } catch (Exception e) {
                        System.err.println("resolverYVerificarUserId: error invocando SesionController." + m + " -> " + e.getMessage());
                    }
                }
            } catch (ClassNotFoundException ignored) {
                System.err.println("resolverYVerificarUserId: SesionController no encontrada");
            }
        }

        return userId;
    }

}