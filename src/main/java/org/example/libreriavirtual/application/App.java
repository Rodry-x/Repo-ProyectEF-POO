package org.example.libreriavirtual.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.libreriavirtual.utilities.Path;
import org.example.libreriavirtual.utilities.SesionController;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Restaurar sesión persistida antes de cargar UI
        System.out.println("[App] cargando sesión persistida al iniciar aplicación");
        SesionController.cargarSesionPersistida();

        // registrar estado de sesión
        Integer profesorId = SesionController.getProfesorIdActivo();
        Integer estudianteId = SesionController.getEstudianteIdActivo();
        System.out.println("[App] Sesión -> profesorId=" + profesorId + " estudianteId=" + estudianteId);

        // elegir pantalla inicial según sesión: profesor > estudiante > login profesor
        String inicioFxml;
        if (profesorId != null) {
            inicioFxml = Path.PANEL_PRINCIPAL_FXML;
        } else if (estudianteId != null) {
            inicioFxml = Path.PANEL_ESTUDIANTES_FXML;
        } else {
            inicioFxml = Path.PANEL_LOGIN_PROFESOR_FXML;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(inicioFxml));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 410, 550);
        stage.setTitle("Panel Principal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}