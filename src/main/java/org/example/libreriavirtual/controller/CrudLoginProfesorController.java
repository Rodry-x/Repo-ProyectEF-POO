package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudLoginProfesorController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    private TextField txtContrasenia;
    @FXML
    private TextField txtEmail;


    @FXML
    void cambiarAlPanelPrincipal(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PRINCIPAL_FXML);
    }

    @FXML
    void iniciarSesion(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
        LimpiarCasillasController.limpiarTextField(txtContrasenia, txtEmail);
    }

}
