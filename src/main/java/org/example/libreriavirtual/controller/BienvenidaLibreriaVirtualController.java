package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class BienvenidaLibreriaVirtualController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    void cambiarAEstanteria(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTANTERIA_FXML);
    }
    @FXML
    void cambiarAPanelLoginAdmin(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_PROFESOR_FXML);
    }
    @FXML
    void cambiarAlPanelLoginEstudiante(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }
}
