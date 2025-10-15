package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudSeccionesController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    private ComboBox<?> cmbGrado;

    @FXML
    private ComboBox<?> cmbSeccion;

    @FXML
    private Label lblContraseniaEstudianteNew;

    @FXML
    private Label lblCorreoEstudianteNew;

    @FXML
    private TextField txtApellidosEstudiante;

    @FXML
    private TextField txtGrado;

    @FXML
    private TextField txtNombreEstudiante;

    @FXML
    private TextField txtSeccion;

    @FXML
    void cambiarAlPanelAula(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_AULA_FXML);
    }

    @FXML
    void enviarLosDatosEstudiante(ActionEvent event) {

    }

    @FXML
    void enviarLosDatosSeccion(ActionEvent event) {

    }

    @FXML
    void limpiarCasillasEstudiante(ActionEvent event) {
        LimpiarCasillasController.limpiarComboBox(cmbGrado, cmbSeccion);
        LimpiarCasillasController.limpiarTextField(txtApellidosEstudiante, txtNombreEstudiante);
    }

    @FXML
    void limpiarCasillasSecciones(ActionEvent event) {
        LimpiarCasillasController.limpiarTextField(txtGrado, txtSeccion);
    }

}