package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.database.DataBaseController;
import org.example.libreriavirtual.database.SesionController;
import org.example.libreriavirtual.utilities.MostrarAlerta;
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
        // Obtener los valores de los campos de texto
        String email = txtEmail.getText();
        String contrasenia = txtContrasenia.getText();

        // Validar el login del profesor
        DataBaseController db = new DataBaseController();
        DataBaseController.ResultadoLogin resultado = db.validarLoginProfesor(email, contrasenia);

        switch (resultado) {
            case EXITO:
                int idProfesor = db.obtenerIdProfesorPorEmail(email);
                SesionController.setIdProfesor(idProfesor);
                sceneController.cambiarEscena(event, Path.PANEL_PROFESOR_FXML);
                break;
            case CONTRASENIA_INCORRECTA:
                MostrarAlerta.error("Error", "La contraseña y/o el email no coincide.");
                break;
            case EMAIL_NO_EXISTE:
                MostrarAlerta.error("Error", "No existe el email.");
                break;
            case ERROR_BD:
                MostrarAlerta.error("Error", "Error de conexión a la base de datos.");
                break;
        }
    }
}
