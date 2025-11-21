package org.example.libreriavirtual.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.SesionController;
import org.example.libreriavirtual.model.*;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudLoginProfesorController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    private static final Gson gson = new Gson();

    @FXML
    private TextField txtContrasenia;
    @FXML
    private TextField txtEmail;


    @FXML
    void cambiarAlPanelPrincipal(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PRINCIPAL_FXML);
    }

    @FXML
    void iniciarSesion(ActionEvent event) {
        // Obtener los valores de los campos de texto
        String email = txtEmail.getText();
        String password = txtContrasenia.getText();

        PostLogin body = new PostLogin(email, password);
        String jsonBody = gson.toJson(body);

        try (var response = ApiClient.request("/auth/login", "POST", jsonBody)) {

            String responseBody = response.body().string();

            // Imprimir el código de respuesta y el cuerpo para depuración
            System.out.println("Response code: " + response.code());
            System.out.println("Response body: " + responseBody);

            if (response.code() == 200) {
                JsonObject responseObject = gson.fromJson(responseBody, JsonObject.class);
                User user = gson.fromJson(responseObject.get("user").toString(), User.class);

                // Iniciar sesión guardando el usuario actual
                SesionController.iniciarSesion(user);

                // Trazas de depuración: id, role y existencia del archivo de sesión
                System.out.println("[Login] user.id = " + (user != null ? user.getId() : "null"));
                System.out.println("[Login] user.role = " + (user != null ? user.getRole() : "null"));
                System.out.println("[Login] Sesión persistida existe? " + SesionController.sesionPersistidaExiste());
                System.out.println("[Login] Ruta de sesión: " + SesionController.getSessionFilePath());


                // Mostrar una alerta de éxito
                MostrarAlerta.info("Inicio de sesión exitoso", "¡Bienvenido/a " + user.getFull_name() + "!");

                // Cambiar a la escena del panel principal
                sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
            } else {
                // Mostrar una alerta de error si el inicio de sesión falla
                MostrarAlerta.error("Error de inicio de sesión", "Credenciales inválidas. Por favor, inténtalo de nuevo.");
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
