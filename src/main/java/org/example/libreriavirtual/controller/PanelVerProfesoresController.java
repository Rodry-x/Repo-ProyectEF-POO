package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class PanelVerProfesoresController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();


    @FXML
    private Label lblSeccion;

    @FXML
    private ListView<?> lstVerProfesores;

    @FXML
    void cambiarAlPanelEstudiantes(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTUDIANTES_FXML);
    }

    @FXML
    void setVerProfesorIniciar(ActionEvent event) {

    }

}