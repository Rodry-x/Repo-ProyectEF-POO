package org.example.libreriavirtual.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.libreriavirtual.utilities.Path;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(Path.PANEL_PRINCIPAL_FXML));
        Scene scene = new Scene(fxmlLoader.load(), 410, 550);
        stage.setTitle("Panel Principal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}