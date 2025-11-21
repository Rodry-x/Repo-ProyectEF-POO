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
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.SesionController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class CrudCambiarContraseniaProfesorController implements Initializable {

    private final SceneController sceneController = new SceneController();
    private static final Gson gson = new Gson();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^.+@.+\\..+$");

    @FXML
    private Label lblEmail;

    @FXML
    private TextField txtContrasenia;

    @FXML
    private TextField txtEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SesionController.cargarSesionPersistida();

        Integer profId = SesionController.getProfesorIdActivo();
        System.out.println("[CrudCambiarContraseniaProfesor] profId: " + profId);

        if (profId != null) {
            User u = SesionController.getProfesorActivo();
            if (u == null) {
                System.out.println("[CrudCambiarContraseniaProfesor] Usuario en memoria no encontrado, intentando sincronizar desde API...");
                SesionController.sincronizarProfesor(profId);
                u = SesionController.getProfesorActivo();
            }
            if (u != null && u.getEmail() != null && !u.getEmail().isBlank()) {
                lblEmail.setText(u.getEmail());
                txtEmail.setText(u.getEmail());
                return;
            } else {
                System.out.println("[CrudCambiarContraseniaProfesor] Usuario sincronizado es null o no tiene email.");
            }
        } else {
            System.out.println("[CrudCambiarContraseniaProfesor] No hay profesorId activo.");
        }
        lblEmail.setText("No disponible");
    }

    @FXML
    void enviarContraseniaNueva(ActionEvent event) {
        String nueva = txtContrasenia.getText() != null ? txtContrasenia.getText().trim() : "";
        if (nueva.length() < 8) {
            MostrarAlerta.error("Validación", "La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        Integer profId = SesionController.getProfesorIdActivo();
        if (profId == null) {
            MostrarAlerta.error("Error", "No hay profesor logueado.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("password", nueva);
        // agregar password_confirmation por si la API lo requiere
        body.addProperty("password_confirmation", nueva);

        sendPatchWithFallback(profId, body, () -> {
            Platform.runLater(() -> {
                txtContrasenia.clear();
                MostrarAlerta.info("Éxito", "Contraseña actualizada correctamente.");
            });
        });
    }

    @FXML
    void enviarCorreoNuevo(ActionEvent event) {
        String nuevo = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        if (nuevo.isEmpty() || !EMAIL_PATTERN.matcher(nuevo).matches()) {
            MostrarAlerta.error("Validación", "Ingrese un correo válido.");
            return;
        }

        Integer profId = SesionController.getProfesorIdActivo();
        if (profId == null) {
            MostrarAlerta.error("Error", "No hay profesor logueado.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("email", nuevo);
        // algunas APIs requieren la contraseña para cambiar el email; si la tienes, agrégala aquí:
        // body.addProperty("current_password", currentPassword);

        sendPatchWithFallback(profId, body, () -> {
            Platform.runLater(() -> {
                lblEmail.setText(nuevo);
                MostrarAlerta.info("Éxito", "Correo actualizado correctamente.");
            });
        });
    }

    // intenta varias combinaciones hasta obtener éxito o reportar el error
    private void sendPatchWithFallback(Integer id, JsonObject payload, Runnable onSuccess) {
        CompletableFuture.runAsync(() -> {
            try {
                // 1) PATCH con payload directo
                if (tryRequest("/users/" + id, "PATCH", gson.toJson(payload), onSuccess)) return;

                // 2) PATCH con wrapper { "user": payload }
                JsonObject wrapper = new JsonObject();
                wrapper.add("user", payload);
                if (tryRequest("/users/" + id, "PATCH", gson.toJson(wrapper), onSuccess)) return;

                // 3) PUT con wrapper (fallback común en algunas APIs)
                if (tryRequest("/users/" + id, "PUT", gson.toJson(wrapper), onSuccess)) return;

                // si llegó aquí, todas fallaron: mostrar último error ya registrado en tryRequest
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar usuario: " + e.getMessage()));
            }
        });
    }

    // realiza la petición y procesa la respuesta; devuelve true si fue exitosa y se ejecutó onSuccess
    private boolean tryRequest(String path, String method, String jsonBody, Runnable onSuccess) {
        try (Response resp = ApiClient.request(path, method, jsonBody)) {
            if (resp == null) {
                System.out.println("[CrudCambiarContraseniaProfesor] Response nula para " + method + " " + path);
                return false;
            }

            String respBodyTmp = "";
            try {
                respBodyTmp = resp.body() != null ? resp.body().string() : "";
            } catch (Exception ex) {
                System.out.println("[CrudCambiarContraseniaProfesor] No se pudo leer body: " + ex.getMessage());
            }
            final String respBody = respBodyTmp;

            System.out.println("[CrudCambiarContraseniaProfesor] " + method + " " + path + " -> HTTP " + resp.code() + " / body: " + respBody);

            // analizar payload solicitado para validar resultado (especialmente email)
            String requestedEmail = null;
            boolean requestedPassword = false;
            try {
                JsonObject sent = gson.fromJson(jsonBody, JsonObject.class);
                if (sent != null) {
                    if (sent.has("user") && sent.get("user").isJsonObject()) {
                        sent = sent.getAsJsonObject("user");
                    }
                    if (sent.has("email")) {
                        requestedEmail = sent.get("email").getAsString();
                    }
                    if (sent.has("password") || sent.has("password_confirmation")) {
                        requestedPassword = true;
                    }
                }
            } catch (Exception ex) {
                // ignore parsing errors
            }

            if (resp.isSuccessful()) {
                // si pedimos cambiar email, comprobar que la respuesta refleje el nuevo email
                if (requestedEmail != null) {
                    try {
                        JsonObject respJson = respBody != null && !respBody.isBlank() ? gson.fromJson(respBody, JsonObject.class) : null;
                        String returnedEmail = null;
                        if (respJson != null) {
                            if (respJson.has("email")) {
                                returnedEmail = respJson.get("email").getAsString();
                            } else if (respJson.has("user") && respJson.get("user").isJsonObject()) {
                                JsonObject u = respJson.getAsJsonObject("user");
                                if (u.has("email")) returnedEmail = u.get("email").getAsString();
                            }
                        }
                        if (returnedEmail != null && returnedEmail.equals(requestedEmail)) {
                            if (onSuccess != null) onSuccess.run();
                            return true;
                        } else {
                            System.out.println("[CrudCambiarContraseniaProfesor] La respuesta no refleja el nuevo email (esperado=" + requestedEmail + ", recibido=" + returnedEmail + "). Intentando siguiente alternativa.");
                            return false; // tratar como fallo para probar wrapper/PUT
                        }
                    } catch (Exception ex) {
                        System.out.println("[CrudCambiarContraseniaProfesor] No se pudo parsear respuesta para validar email: " + ex.getMessage());
                        return false;
                    }
                }

                // para password: considerar éxito si no hay clave "errors" en la respuesta
                if (requestedPassword) {
                    try {
                        JsonObject respJson = respBody != null && !respBody.isBlank() ? gson.fromJson(respBody, JsonObject.class) : null;
                        if (respJson != null && respJson.has("errors")) {
                            System.out.println("[CrudCambiarContraseniaProfesor] Response contiene errores: " + respBody);
                            Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al actualizar contraseña: " + respBody));
                            return false;
                        } else {
                            if (onSuccess != null) onSuccess.run();
                            return true;
                        }
                    } catch (Exception ex) {
                        // si no se pudo parsear, asumir éxito si código 2xx
                        if (onSuccess != null) onSuccess.run();
                        return true;
                    }
                }

                // caso general: éxito
                if (onSuccess != null) onSuccess.run();
                return true;
            } else {
                // Mostrar error detallado al usuario (en UI thread)
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

    // método opcional no usado actualmente
    private void patchUser(Integer id, JsonObject payload, Runnable onSuccess) {
        sendPatchWithFallback(id, payload, onSuccess);
    }

    @FXML
    void cambiarAlPanelConfiguracionProfesor(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CONFIGURACION_PROFESOR_FXML);
    }
}