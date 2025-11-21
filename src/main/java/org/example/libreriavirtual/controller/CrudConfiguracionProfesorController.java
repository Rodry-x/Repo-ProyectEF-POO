package org.example.libreriavirtual.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.utilities.SesionController;
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
    void CerrarSesionProfesor(MouseEvent event) {
        // Limpiar sesión en memoria y en disco
        SesionController.cerrarSesionProfesor();
        SesionController.borrarSesionPersistida();

        // Informar al usuario y volver al login
        MostrarAlerta.info("Sesión cerrada", "La sesión se ha cerrado correctamente.");
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_PROFESOR_FXML);
    }

}