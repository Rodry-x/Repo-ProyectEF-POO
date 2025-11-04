package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.service.ApiClient;

public class BienvenidaLibreriaVirtualController {

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
        try {
            var response = ApiClient.request("/users", "GET", null);
            System.out.println("Response code: " + response.code());
            System.out.println("Response body: " + response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
