package org.example.libreriavirtual.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.database.SesionController;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudConfiguracionProfesorController {
    private final SceneController sceneController = new SceneController();

    @FXML
    void cambiarAPanelProfesor(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
    }

    @FXML
    void cambiarAlPanelContrasenia(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CAMBIAR_CONTRASENIA_PROFESOR_FXML);
    }

    @FXML
    void cambiarAlPanelLoginProfesor(MouseEvent event) {
        MostrarAlerta.info("Sesión cerrada", "La sesión se ha cerrado correctamente.");
        SesionController.cerrarSesionProfesor();
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_PROFESOR_FXML);
    }

}