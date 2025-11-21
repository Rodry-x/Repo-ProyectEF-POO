package org.example.libreriavirtual.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import okhttp3.Response;
import org.example.libreriavirtual.model.User;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.ApiUtils;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SesionController;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PanelVerProfesoresController implements Initializable {
    private final SceneController sceneController = new SceneController();

    @FXML
    private Label lblSeccion;

    @FXML
    private Label lblGrado;

    @FXML
    private ListView<String> lstVerProfesores;

    private volatile int currentGradeId = -1;
    private volatile int currentSectionId = -1;

    @FXML
    void cambiarAlPanelEstudiantes(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }

    @Override
    public void initialize(URL location, java.util.ResourceBundle resources) {
        // usar initData al entrar al panel
    }

    // Invocar al abrir la escena
    public void initData(int gradeId, int sectionId) {
        System.out.println("[PanelVerProfesores] initData: gradeId=" + gradeId + " sectionId=" + sectionId);
        this.currentGradeId = gradeId;
        this.currentSectionId = sectionId;

        // si no se recibieron ids válidos, intentar detectar sección del estudiante
        if (this.currentGradeId <= 0 || this.currentSectionId <= 0) {
            User u = SesionController.getUsuarioActual();
            Integer estudianteId = SesionController.getEstudianteIdActivo();
            int[] found = null;
            try {
                if (estudianteId != null && estudianteId > 0) {
                    found = buscarSeccionPorEstudianteId(estudianteId);
                }
                if (found == null && u != null) {
                    // intentar por email como fallback
                    String email = u.getEmail();
                    if (email != null && !email.isBlank()) found = buscarSeccionPorEstudianteEmail(email.trim());
                }
            } catch (Exception e) {
                System.err.println("[PanelVerProfesores] Error detectando sección del estudiante: " + e.getMessage());
            }
            if (found != null) {
                this.currentGradeId = found[0];
                this.currentSectionId = found[1];
                System.out.println("[PanelVerProfesores] sección detectada -> gradeId=" + currentGradeId + " sectionId=" + currentSectionId);
            } else {
                System.out.println("[PanelVerProfesores] no se pudo detectar sección del estudiante, usar valores por defecto");
            }
        }

        cargarGradoYSeccion(currentGradeId, currentSectionId);

        if (currentSectionId <= 0) {
            Platform.runLater(() -> lstVerProfesores.getItems().clear());
            return;
        }

        cargarProfesoresPorSeccion(currentSectionId);
    }

    // Recorre /grades -> /grades/{grade}/sections -> /sections/{section}/students buscando el estudiante por id
    private int[] buscarSeccionPorEstudianteId(int estudianteId) throws IOException {
        JsonArray grades = ApiUtils.getJsonArray("/grades");
        if (grades == null) return null;
        for (JsonElement ge : grades) {
            if (!ge.isJsonObject()) continue;
            JsonObject g = ge.getAsJsonObject();
            Integer gid = safeGetInt(g, "id");
            if (gid == null) continue;

            JsonArray sections = ApiUtils.getJsonArray("/grades/" + gid + "/sections");
            if (sections == null) continue;
            for (JsonElement se : sections) {
                if (!se.isJsonObject()) continue;
                JsonObject s = se.getAsJsonObject();
                Integer sid = safeGetInt(s, "id");
                if (sid == null) continue;

                JsonArray students = ApiUtils.getJsonArray("/sections/" + sid + "/students");
                if (students == null) continue;
                for (JsonElement st : students) {
                    if (!st.isJsonObject()) continue;
                    JsonObject so = st.getAsJsonObject();
                    Integer sidUser = safeGetInt(so, "id");
                    if (sidUser != null && sidUser == estudianteId) {
                        return new int[]{gid, sid};
                    }
                }
            }
        }
        return null;
    }

    // Similar pero busca por email de estudiante
    private int[] buscarSeccionPorEstudianteEmail(String email) throws IOException {
        if (email == null || email.isBlank()) return null;
        JsonArray grades = ApiUtils.getJsonArray("/grades");
        if (grades == null) return null;
        for (JsonElement ge : grades) {
            if (!ge.isJsonObject()) continue;
            JsonObject g = ge.getAsJsonObject();
            Integer gid = safeGetInt(g, "id");
            if (gid == null) continue;

            JsonArray sections = ApiUtils.getJsonArray("/grades/" + gid + "/sections");
            if (sections == null) continue;
            for (JsonElement se : sections) {
                if (!se.isJsonObject()) continue;
                JsonObject s = se.getAsJsonObject();
                Integer sid = safeGetInt(s, "id");
                if (sid == null) continue;

                JsonArray students = ApiUtils.getJsonArray("/sections/" + sid + "/students");
                if (students == null) continue;
                for (JsonElement st : students) {
                    if (!st.isJsonObject()) continue;
                    JsonObject so = st.getAsJsonObject();
                    if (so.has("email") && so.get("email").isJsonPrimitive()) {
                        String e = so.get("email").getAsString();
                        if (email.equalsIgnoreCase(e)) {
                            return new int[]{gid, sid};
                        }
                    }
                }
            }
        }
        return null;
    }

    public void cargarProfesoresPorSeccion(int sectionId) {
        if (sectionId <= 0) {
            Platform.runLater(() -> lstVerProfesores.getItems().clear());
            return;
        }

        lstVerProfesores.getItems().clear();

        Thread th = new Thread(() -> {
            Set<String> profesores = new LinkedHashSet<>();
            try {
                System.out.println("[PanelVerProfesores] solicitando /sections/" + sectionId + "/courses");
                JsonArray cursos = ApiUtils.getJsonArray("/sections/" + sectionId + "/courses");
                if (cursos != null) {
                    for (JsonElement cursoEl : cursos) {
                        if (!cursoEl.isJsonObject()) continue;
                        JsonObject cursoObj = cursoEl.getAsJsonObject();

                        if (!cursoObj.has("created_by")) continue;
                        JsonElement cb = cursoObj.get("created_by");
                        String nombre = resolveCreatedBy(cb);
                        if (nombre != null && !nombre.isBlank()) {
                            profesores.add(nombre);
                        } else {
                            // intentos alternativos desde el curso
                            String alt = extractStringField(cursoObj, "teacher");
                            if (alt != null) profesores.add(alt);
                        }
                    }
                } else {
                    System.err.println("[PanelVerProfesores] No hay cursos para sección " + sectionId);
                }
            } catch (IOException e) {
                System.err.println("[PanelVerProfesores] Error al obtener cursos: " + e.getMessage());
            }

            List<String> lista = new ArrayList<>(profesores);
            Platform.runLater(() -> lstVerProfesores.getItems().setAll(lista));
        });

        th.setDaemon(true);
        th.start();
    }

    // Helper y resolvers (reutilizados)
    private String obtenerNombreDesdeObjeto(JsonObject obj) {
        if (obj == null) return null;
        if (obj.has("name") && obj.get("name").isJsonPrimitive()) return obj.get("name").getAsString();
        if (obj.has("nombre") && obj.get("nombre").isJsonPrimitive()) return obj.get("nombre").getAsString();
        if (obj.has("full_name") && obj.get("full_name").isJsonPrimitive()) return obj.get("full_name").getAsString();
        if (obj.has("fullName") && obj.get("fullName").isJsonPrimitive()) return obj.get("fullName").getAsString();
        if (obj.has("username") && obj.get("username").isJsonPrimitive()) return obj.get("username").getAsString();
        if (obj.has("email") && obj.get("email").isJsonPrimitive()) return obj.get("email").getAsString();
        if (obj.has("label") && obj.get("label").isJsonPrimitive()) return obj.get("label").getAsString();
        if (obj.has("title") && obj.get("title").isJsonPrimitive()) return obj.get("title").getAsString();
        return null;
    }

    private String resolveCreatedBy(JsonElement cb) {
        try {
            if (cb == null || cb.isJsonNull()) return null;

            if (cb.isJsonObject()) {
                JsonObject cbObj = cb.getAsJsonObject();
                String name = obtenerNombreDesdeObjeto(cbObj);
                if (name != null && !name.isBlank()) return name;
                Integer id = safeGetInt(cbObj, "id");
                if (id != null) return obtenerNombreUsuarioPorId(id);
            }

            if (cb.isJsonPrimitive()) {
                try {
                    int uid = cb.getAsInt();
                    return obtenerNombreUsuarioPorId(uid);
                } catch (Exception ignored) {
                    String txt = cb.getAsString();
                    if (txt != null && !txt.isBlank()) return txt;
                }
            }

            if (cb.isJsonArray()) {
                JsonArray arr = cb.getAsJsonArray();
                for (JsonElement el : arr) {
                    String r = resolveCreatedBy(el);
                    if (r != null && !r.isBlank()) return r;
                }
            }
        } catch (Exception e) {
            System.err.println("[PanelVerProfesores] Error al resolver created_by: " + e.getMessage());
        }
        return null;
    }

    private String obtenerNombreUsuarioPorId(int userId) {
        if (userId <= 0) return null;
        Gson gson = new Gson();
        try (Response resp = ApiClient.request("/users/" + userId, "GET", null)) {
            if (resp != null && resp.isSuccessful() && resp.body() != null) {
                String body = resp.body().string();
                JsonElement el = gson.fromJson(body, JsonElement.class);
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    String n = obtenerNombreDesdeObjeto(o);
                    if (n != null && !n.isBlank()) return n;
                    if (o.has("email") && o.get("email").isJsonPrimitive()) return o.get("email").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("[PanelVerProfesores] Error en obtenerNombreUsuarioPorId: " + e.getMessage());
        }

        // fallback a /users completo
        try (Response r2 = ApiClient.request("/users", "GET", null)) {
            if (r2 == null || !r2.isSuccessful() || r2.body() == null) return null;
            String body = r2.body().string();
            JsonArray arr = new Gson().fromJson(body, JsonArray.class);
            if (arr == null) return null;
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject u = el.getAsJsonObject();
                Integer id = safeGetInt(u, "id");
                if (id != null && id == userId) {
                    String n = obtenerNombreDesdeObjeto(u);
                    if (n != null && !n.isBlank()) return n;
                    if (u.has("email") && u.get("email").isJsonPrimitive()) return u.get("email").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("[PanelVerProfesores] Error en fallback /users: " + e.getMessage());
        }
        return null;
    }

    private void cargarGradoYSeccion(int gradeId, int sectionId) {
        Thread th = new Thread(() -> {
            String nombreGrado = null;
            String nombreSeccion = null;
            try {
                if (gradeId > 0) {
                    JsonArray grades = ApiUtils.getJsonArray("/grades");
                    if (grades != null) {
                        for (JsonElement ge : grades) {
                            if (!ge.isJsonObject()) continue;
                            JsonObject g = ge.getAsJsonObject();
                            Integer id = safeGetInt(g, "id");
                            if (id != null && id == gradeId) {
                                nombreGrado = obtenerNombreDesdeObjeto(g);
                                break;
                            }
                        }
                    }

                    JsonArray sections = ApiUtils.getJsonArray("/grades/" + gradeId + "/sections");
                    if (sections != null) {
                        for (JsonElement se : sections) {
                            if (!se.isJsonObject()) continue;
                            JsonObject s = se.getAsJsonObject();
                            Integer sid = safeGetInt(s, "id");
                            if (sid != null && sid == sectionId) {
                                nombreSeccion = obtenerNombreDesdeObjeto(s);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[PanelVerProfesores] Error al cargar grado/seccion: " + e.getMessage());
            }

            final String ng = nombreGrado != null ? nombreGrado : "Grado desconocido";
            final String ns = nombreSeccion != null ? nombreSeccion : "Sección desconocida";
            Platform.runLater(() -> {
                lblGrado.setText(ng);
                lblSeccion.setText(ns);
            });
        });

        th.setDaemon(true);
        th.start();
    }

    private String extractStringField(JsonObject obj, String field) {
        try {
            if (obj.has(field) && obj.get(field).isJsonPrimitive()) return obj.get(field).getAsString();
        } catch (Exception ignored) {}
        return null;
    }

    // Helper seguro para obtener enteros de objetos JSON
    private Integer safeGetInt(JsonObject o, String field) {
        try {
            if (o.has(field) && o.get(field).isJsonPrimitive()) return o.get(field).getAsInt();
        } catch (Exception ignored) {}
        return null;
    }
}