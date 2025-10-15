package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;

public class CrudLibrosController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    @FXML
    private Label lblNombreDelLibro;

    public void setNombreDelLibro(String nombre) {
        lblNombreDelLibro.setText(nombre);
    }

    @FXML
    void cambiarAEstanteria(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTANTERIA_FXML);
    }

}