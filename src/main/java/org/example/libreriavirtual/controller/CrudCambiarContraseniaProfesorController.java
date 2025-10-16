package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.database.DataBaseController;
import org.example.libreriavirtual.database.SesionController;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.MostrarAlerta;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SceneController;

public class CrudCambiarContraseniaProfesorController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();



    @FXML
    private Label lblEmail;

    // Método para inicializar el controlador y cargar el email actual del profesor
    public void initialize() {
        int idProfesor = SesionController.getIdProfesor();
        DataBaseController db = new DataBaseController();
        String emailActual = db.obtenerEmailProfesorPorId(idProfesor);
        lblEmail.setText(emailActual);
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
        int idProfesor = SesionController.getIdProfesor();

        // Actualizar la contraseña en la base de datos
        DataBaseController db = new DataBaseController();

        boolean exito = db.actualizarContraseniaProfesor(idProfesor, nuevaContrasenia);
        if (exito) {
            MostrarAlerta.info("Éxito", "Contraseña actualizada correctamente.");
            LimpiarCasillasController.limpiarTextField(txtContrasenia);
        } else {
            MostrarAlerta.error("Error", "No se pudo actualizar la contraseña.");
        }
    }

    @FXML
    void enviarCorreoNuevo(ActionEvent event) {
        String nuevoEmail = txtEmail.getText();
        int idProfesor = SesionController.getIdProfesor();
        DataBaseController db = new DataBaseController();

        boolean exito = db.actualizarEmailProfesor(idProfesor, nuevoEmail);
        if (exito) {
            MostrarAlerta.info("Éxito", "Email actualizado correctamente.");
            LimpiarCasillasController.limpiarTextField(txtEmail);
            sceneController.cambiarEscena(event, Path.PANEL_CAMBIAR_CONTRASENIA_PROFESOR_FXML);
        } else {
            MostrarAlerta.error("Error", "No se pudo actualizar el email.");
        }
    }

}