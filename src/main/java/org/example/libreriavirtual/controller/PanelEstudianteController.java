package org.example.libreriavirtual.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.libreriavirtual.utilities.*;

import java.io.IOException;

public class PanelEstudianteController {

    SceneController sceneController = new SceneController();

    @FXML
    public void initialize() {
        // Cargar sesión persistida al inicializar el controlador JavaFX
        SesionController.cargarSesionPersistida();
    }

    @FXML
    void cambiarAEstanteriaVirtual(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_ESTANTERIA_FXML);
    }

    @FXML
    void cambiarAPanelConfiguracion(MouseEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_CONFIGURACION_ESTUDIANTE_FXML);
    }

    @FXML
    void cambiarAPanelVerProfesores(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Path.PANEL_VER_PROFESORES_FXML));
            Parent root = loader.load();

            // Obtener controlador y pasar datos de contexto
            var controllerObj = loader.getController();

            // Obtener gradeId/sectionId desde SesionController (nuevos métodos)
            int gradeId = -1;
            int sectionId = -1;
            Integer gid = SesionController.getGradeIdActivo();
            Integer sid = SesionController.getSectionIdActivo();
            if (gid != null) gradeId = gid;
            if (sid != null) sectionId = sid;

            // Castear al controlador correcto antes de llamar initData
            if (controllerObj instanceof PanelVerProfesoresController) {
                PanelVerProfesoresController controller = (PanelVerProfesoresController) controllerObj;
                controller.initData(gradeId, sectionId);
            }

            // Mostrar la nueva vista en la misma ventana
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cambiarAPanelLoginEstudiante(ActionEvent event) {

        // Cerrar sesión localmente y borrar sesión persistida
        try {
            SesionController.cerrarSesionEstudiante();
            SesionController.borrarSesionPersistida();
        } catch (Exception ex) {
            System.err.println("[CrudConfiguracionEstudiante] Error cerrando sesión: " + ex.getMessage());
        }
        // Navegar al panel de login para estudiantes
        sceneController.cambiarEscena(event, Path.PANEL_LOGIN_ESTUDIANTE_FXML);
    }

}