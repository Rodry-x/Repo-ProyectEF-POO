package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class PanelEstudianteController {

    SceneController sceneController = new SceneController();

    @FXML
    void cambiarAEstanteriaVirtual(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTANTERIA_FXML);
    }

    @FXML
    void cambiarAPanelConfiguracion(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CONFIGURACION_ESTUDIANTE_FXML);
    }

    @FXML
    void cambiarAPanelVerProfesores(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_VER_PROFESORES_FXML);
    }

    @FXML
    void cambiarAPanelLoginEstudiante(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }

}