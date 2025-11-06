package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class CrudCambiarContraseniaProfesorController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();



    @FXML
    private Label lblEmail;

    // Método para inicializar el controlador y cargar el email actual del profesor
    private void initialize() {
        //
    }

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
        // Obtener la nueva contraseña del campo de texto
        String nuevaContrasenia = txtContrasenia.getText();

        // Obtener el ID del profesor desde la sesión


        // Actualizar la contraseña en la base de datos

    }

    @FXML
    void enviarCorreoNuevo(ActionEvent event) {
        //hacer la logica para cambiar el correo
    }
}