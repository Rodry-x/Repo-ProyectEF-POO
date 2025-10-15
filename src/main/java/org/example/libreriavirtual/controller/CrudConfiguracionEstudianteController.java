package org.example.libreriavirtual.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudConfiguracionEstudianteController {
    private final SceneController sceneController = new SceneController();

    @FXML
    void cambiarAlPanelContrasenia(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CAMBIAR_CONTRASENIA_ESTUDIANTE_FXML);
    }

    @FXML
    void cambiarAPanelEstudiante(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }

    @FXML
    void cambiarAlPanelLoginEstudiantes(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }

}