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
        SesionController.cargarSesionPersistida();

        // elegir pantalla inicial según si hay profesor logueado
        String inicioFxml = (SesionController.getProfesorIdActivo() != null)
                ? Path.PANEL_PRINCIPAL_FXML
                : Path.PANEL_LOGIN_PROFESOR_FXML;

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