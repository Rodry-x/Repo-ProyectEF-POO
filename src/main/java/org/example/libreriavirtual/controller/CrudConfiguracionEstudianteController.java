package org.example.libreriavirtual.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SesionController;

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
    void cerrarSesionEstudiante(MouseEvent event) {

        // Cerrar sesión localmente y borrar sesión persistida
        try {
            SesionController.cerrarSesionEstudiante();
            SesionController.borrarSesionPersistida();
        } catch (Exception ex) {
            System.err.println("[CrudConfiguracionEstudiante] Error cerrando sesión: " + ex.getMessage());
        }
        // Navegar al panel de login para estudiantes
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }

}