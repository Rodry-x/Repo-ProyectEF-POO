package org.example.libreriavirtual.utilities;

import javafx.scene.control.TextField;

public class LimpiarCasillasController {

    public static void limpiarTextField(TextField... campos) {
        for (TextField campo : campos) {
            if (campo != null) {
                campo.setText("");
            }
        }
    }

    public static void limpiarComboBox(javafx.scene.control.ComboBox<?>... combos) {
        for (javafx.scene.control.ComboBox<?> combo : combos) {
            if (combo != null && combo.getItems().size() > 0) {
                combo.getSelectionModel().select(0);
            }
        }
    }
}
