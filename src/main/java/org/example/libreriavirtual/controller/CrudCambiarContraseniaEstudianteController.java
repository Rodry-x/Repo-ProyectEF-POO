package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class CrudCambiarContraseniaEstudianteController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();



    @FXML
    private Label lblEmail;

    @FXML
    private TextField txtContraseniaNew;

    @FXML
    void actualizandoNuevaContrasenia(ActionEvent event) {
        LimpiarCasillasController.limpiarTextField(txtContraseniaNew);
    }

    @FXML
    void cambiarAlPanelEstudiantes(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }
}
