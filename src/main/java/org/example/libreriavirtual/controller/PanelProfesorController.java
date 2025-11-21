package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.utilities.SesionController;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class PanelProfesorController {

    SceneController sceneController = new SceneController();

    @FXML
    public void initialize() {
        // Cargar sesión persistida al inicializar el controlador JavaFX
        SesionController.cargarSesionPersistida();
    }

    @FXML
    void cambiarAPanelAulaVirtual(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_AULA_FXML);
    }

    @FXML
    void cambiarAPanelConfiguracion(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CONFIGURACION_PROFESOR_FXML);
    }

    @FXML
    void CerrarSesionProfesor(ActionEvent event) {
        // Limpiar sesión en memoria y en disco
        SesionController.cerrarSesionProfesor();
        SesionController.borrarSesionPersistida();

        // Informar al usuario y volver al login
        MostrarAlerta.info("Sesión cerrada", "La sesión se ha cerrado correctamente.");
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_PROFESOR_FXML);
    }

}