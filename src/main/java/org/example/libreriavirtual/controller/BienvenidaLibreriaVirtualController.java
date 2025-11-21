package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.libreriavirtual.model.PostLogin;
import org.example.libreriavirtual.model.PostUser;
import org.example.libreriavirtual.model.User;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.service.ApiClient;

public class BienvenidaLibreriaVirtualController {

    private static final Gson gson = new Gson();

    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    void cambiarAPanelLoginAdmin(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_PROFESOR_FXML);
    }

    @FXML
    void cambiarAlPanelLoginEstudiante(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }

    @FXML
    void probarRequest(ActionEvent event) {

            PostUser body = new PostUser("Docente Prueba", "prueba@example.com", "123456789", "teacher");
            String jsonBody = gson.toJson(body);

            try (var response = ApiClient.request("/users", "POST", jsonBody)) {

                String responseBody = response.body().string();

                System.out.println("Response code: " + response.code());
                System.out.println("Response body: " + responseBody);

                JsonObject responseObject = gson.fromJson(responseBody, JsonObject.class);

                User user = gson.fromJson(responseObject.get("user").toString(), User.class);
                System.out.println(user.getFull_name());
            }

            catch (Exception e) {
                e.printStackTrace();
            }
    }
}
