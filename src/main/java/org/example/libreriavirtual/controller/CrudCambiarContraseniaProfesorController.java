package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class CrudCambiarContraseniaProfesorController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();



    @FXML
    private Label lblEmail;

    @FXML
    private TextField txtContrasenia;

    @FXML
    private TextField txtEmail;

    @FXML
    void cambiarAlPanelConfiguracionProfesor(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CONFIGURACION_PROFESOR_FXML);
    }

    @FXML
    void enviarContraseniaNueva(ActionEvent event) {
        LimpiarCasillasController.limpiarTextField(txtContrasenia);
    }

    @FXML
    void enviarCorreoNuevo(ActionEvent event) {
        LimpiarCasillasController.limpiarTextField(txtEmail);
    }

}