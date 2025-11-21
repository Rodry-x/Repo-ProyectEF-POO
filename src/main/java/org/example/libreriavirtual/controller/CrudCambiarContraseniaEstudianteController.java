package org.example.libreriavirtual.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import okhttp3.Response;
import org.example.libreriavirtual.model.User;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CrudCambiarContraseniaEstudianteController implements Initializable {

    private final SceneController sceneController = new SceneController();
    private static final Gson gson = new Gson();

    @FXML
    private Label lblEmail;

    @FXML
    private TextField txtContraseniaNew;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // intentar cargar sesión y mostrar email del estudiante si está disponible
        SesionController.cargarSesionPersistida();
        Integer estId = SesionController.getEstudianteIdActivo();
        System.out.println("[CrudCambiarContraseniaEstudiante] estudianteId: " + estId);

        if (estId != null) {
            User u = SesionController.getEstudianteActivo();
            if (u == null) {
                System.out.println("[CrudCambiarContraseniaEstudiante] Usuario en memoria no encontrado, intentando sincronizar desde API...");
                // suponer que existe sincronizarEstudiante similar a sincronizarProfesor
                try { SesionController.sincronizarEstudiante(estId); } catch (Exception ignored) {}
                u = SesionController.getEstudianteActivo();
            }
            if (u != null && u.getEmail() != null && !u.getEmail().isBlank()) {
                lblEmail.setText(u.getEmail());
                return;
            } else {
                System.out.println("[CrudCambiarContraseniaEstudiante] Usuario sincronizado es null o no tiene email.");
            }
        } else {
            System.out.println("[CrudCambiarContraseniaEstudiante] No hay estudianteId activo.");
        }
        lblEmail.setText("No disponible");
    }

    @FXML
    void cambiarAlPanelEstudiantes(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }

    @FXML
    void enviarContraseniaNueva(ActionEvent event) {
        String nueva = txtContraseniaNew.getText() != null ? txtContraseniaNew.getText().trim() : "";
        if (nueva.length() < 8) {
            MostrarAlerta.error("Validación", "La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        Integer estId = SesionController.getEstudianteIdActivo();
        if (estId == null) {
            MostrarAlerta.error("Error", "No hay estudiante logueado.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("password", nueva);
        body.addProperty("password_confirmation", nueva);

        sendPatchWithFallback(estId, body, () -> Platform.runLater(() -> {
            txtContraseniaNew.clear();
            MostrarAlerta.info("Éxito", "Contraseña actualizada correctamente.");
        }));
    }

    // similar al controlador de profesor: intenta PATCH directo, PATCH con wrapper { user: ... }, y PUT con wrapper
    private void sendPatchWithFallback(Integer id, JsonObject payload, Runnable onSuccess) {
        CompletableFuture.runAsync(() -> {
            try {
                if (tryRequest("/users/" + id, "PATCH", gson.toJson(payload), onSuccess)) return;

                JsonObject wrapper = new JsonObject();
                wrapper.add("user", payload);
                if (tryRequest("/users/" + id, "PATCH", gson.toJson(wrapper), onSuccess)) return;

                if (tryRequest("/users/" + id, "PUT", gson.toJson(wrapper), onSuccess)) return;

                // si todas fallan, ya se mostró el error en tryRequest
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar usuario: " + e.getMessage()));
            }
        });
    }

    private boolean tryRequest(String path, String method, String jsonBody, Runnable onSuccess) {
        try (Response resp = ApiClient.request(path, method, jsonBody)) {
            if (resp == null) {
                System.out.println("[CrudCambiarContraseniaEstudiante] Response nula para " + method + " " + path);
                return false;
            }

            String respBodyTmp = "";
            try { respBodyTmp = resp.body() != null ? resp.body().string() : ""; } catch (Exception ex) { System.out.println("[CrudCambiarContraseniaEstudiante] No se pudo leer body: " + ex.getMessage()); }
            final String respBody = respBodyTmp;

            System.out.println("[CrudCambiarContraseniaEstudiante] " + method + " " + path + " -> HTTP " + resp.code() + " / body: " + respBody);

            // para password: considerar éxito si no hay clave "errors" en la respuesta o código 2xx
            boolean requestedPassword = false;
            try {
                JsonObject sent = gson.fromJson(jsonBody, JsonObject.class);
                if (sent != null) {
                    if (sent.has("user") && sent.get("user").isJsonObject()) sent = sent.getAsJsonObject("user");
                    if (sent.has("password") || sent.has("password_confirmation")) requestedPassword = true;
                }
            } catch (Exception ignored) {}

            if (resp.isSuccessful()) {
                if (requestedPassword) {
                    try {
                        JsonObject respJson = respBody != null && !respBody.isBlank() ? gson.fromJson(respBody, JsonObject.class) : null;
                        if (respJson != null && respJson.has("errors")) {
                            System.out.println("[CrudCambiarContraseniaEstudiante] Response contiene errores: " + respBody);
                            Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar contraseña: " + respBody));
                            return false;
                        } else {
                            if (onSuccess != null) onSuccess.run();
                            return true;
                        }
                    } catch (Exception ex) {
                        if (onSuccess != null) onSuccess.run();
                        return true;
                    }
                }
                if (onSuccess != null) onSuccess.run();
                return true;
            } else {
                final String bodyForUI = respBody;
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar usuario: HTTP " + resp.code() + " - " + bodyForUI));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar usuario: " + e.getMessage()));
            return false;
        }
    }
}