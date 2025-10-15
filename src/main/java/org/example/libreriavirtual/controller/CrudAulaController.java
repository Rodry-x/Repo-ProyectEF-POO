package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class CrudAulaController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    private ListView<?> lstSecciones;

    @FXML
    void agregarSeccion(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_SECCIONES_FXML);
    }

    @FXML
    void eliminarSeccion(ActionEvent event) {

    }
    @FXML
    void cambiarAlPanelProfesor(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
    }

    @FXML
    void verDetallesDeSecciones(ActionEvent event) {

    }

}