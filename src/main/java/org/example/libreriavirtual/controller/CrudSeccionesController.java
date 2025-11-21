package org.example.libreriavirtual.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.model.*;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.*;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CrudSeccionesController {
    //Usando la clase SceneController para cambiar de escenas
    private final SceneController sceneController = new SceneController();

    //Instanciando Gson
    private static final Gson gson = new Gson();

    //Mapas para resolver nombre -> id
    private final Map<String, Integer> gradeIds = new HashMap<>();
    private final Map<String, Integer> sectionIds = new HashMap<>();

    @FXML
    private ComboBox<String> cmbGrado;

    @FXML
    private ComboBox<String> cmbSeccion;

    @FXML
    private Label lblContraseniaEstudianteNew;

    @FXML
    private Label lblCorreoEstudianteNew;

    @FXML
    private TextField txtApellidosEstudiante;

    @FXML
    private TextField txtGrado;

    @FXML
    private TextField txtNombreEstudiante;

    @FXML
    private TextField txtSeccion;

    @FXML
    public void initialize() {
        // cargar grados al iniciar
        cargarGrados();

        // cuando el usuario seleccione un grado, cargar sus secciones
        cmbGrado.setOnAction(event -> {
            String selected = cmbGrado.getValue();
            Integer id = gradeIds.get(selected);
            if (id != null) {
                cargarSeccionesPorGrado(id);
            } else {
                cmbSeccion.getItems().clear();
            }
        });
    }

    @FXML
    void cambiarAlPanelAula(ActionEvent event) {
        sceneController.cambiarEscena(event, Path.PANEL_AULA_FXML);
    }

    @FXML
    void enviarLosDatosEstudiante(ActionEvent event) {
        // validar sección seleccionada
        String selectedSectionName = cmbSeccion.getValue();
        if (selectedSectionName == null || selectedSectionName.isBlank()) {
            MostrarAlerta.error("Validación", "Seleccione una sección antes de matricular al estudiante.");
            return;
        }
        Integer sectionId = sectionIds.get(selectedSectionName);
        if (sectionId == null) {
            MostrarAlerta.error("Validación", "No se pudo resolver el id de la sección seleccionada.");
            return;
        }
        //Generar email y contraseña automáticamente
        // leer nombre y apellidos
        String nombre = txtNombreEstudiante.getText() != null ? txtNombreEstudiante.getText().trim() : "";
        String apellidos = txtApellidosEstudiante.getText() != null ? txtApellidosEstudiante.getText().trim() : "";
        String fullName = (nombre + " " + apellidos).trim();

        // generar email y contraseña aquí mismo
        String email = CredentialUtils.buildEmailFromName(nombre, apellidos);
        String password = CredentialUtils.generateRandomPassword(10);

        // actualizar labels en la UI
        lblCorreoEstudianteNew.setText(email);
        lblContraseniaEstudianteNew.setText(password);

        PostStudent postStudent = new PostStudent(fullName, email, "student", password);
        String jsonBody = gson.toJson(postStudent);

        try {
            // usar ApiUtils para crear y obtener id del estudiante
            Integer studentId = ApiUtils.postAndExtractId("/users", jsonBody);

            Map<String, Integer> enrollBody = Map.of("student_id", studentId);
            String enrollJson = gson.toJson(enrollBody);
            String enrollPath = "/sections/" + sectionId + "/enroll";

            try (var resp2 = ApiClient.request(enrollPath, "POST", enrollJson)) {
                String enrollRespBody = resp2.body() != null ? resp2.body().string() : "";
                System.out.println("Enroll Response code: " + resp2.code());
                System.out.println("Enroll Response body: " + enrollRespBody);

                String selectedGrade = cmbGrado.getValue();
                if (selectedGrade != null) {
                    Integer gradeId = gradeIds.get(selectedGrade);
                    if (gradeId != null) {
                        cargarSeccionesPorGrado(gradeId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                MostrarAlerta.error("Error", "Fallo al matricular al estudiante: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            MostrarAlerta.error("Error", "Fallo al crear el estudiante: " + e.getMessage());
        }
    }

    @FXML
    void enviarLosDatosSeccion(ActionEvent event) {
        try {
            // crear grado y obtener id usando ApiUtils
            PostGrade body = new PostGrade(txtGrado.getText());
            String jsonBody = gson.toJson(body);
            Integer gradeId = ApiUtils.postAndExtractId("/grades", jsonBody);

            // crear sección usando ApiUtils (si la API devuelve id, lo recibimos; si no, adaptar)
            PostSecciones bodySeccion = new PostSecciones(txtSeccion.getText());
            String jsonBodySeccion = gson.toJson(bodySeccion);
            Integer sectionCreatedId = ApiUtils.postAndExtractId("/grades/" + gradeId + "/sections", jsonBodySeccion);

            // Si necesitas usar el id de la sección creada, sectionCreatedId lo contiene.
            sceneController.cambiarEscena(event, Path.PANEL_SECCIONES_FXML);

        } catch (Exception e) {
            e.printStackTrace();
            MostrarAlerta.error("Error", "Fallo al crear grado/sección: " + e.getMessage());
        }
    }

    @FXML
    void limpiarCasillasEstudiante(ActionEvent event) {
        LimpiarCasillasController.limpiarComboBox(cmbGrado, cmbSeccion);
        LimpiarCasillasController.limpiarTextField(txtApellidosEstudiante, txtNombreEstudiante);
    }

    @FXML
    void limpiarCasillasSecciones(ActionEvent event) {
        LimpiarCasillasController.limpiarTextField(txtGrado, txtSeccion);
    }

    // Cargar todos los grados desde la API y rellenar cmbGrado
    private void cargarGrados() {
        new Thread(() -> {
            try {
                GradeResponse[] grades = ApiUtils.getArray("/grades", GradeResponse[].class);
                Platform.runLater(() -> {
                    gradeIds.clear();
                    cmbGrado.getItems().clear();
                    if (grades != null) {
                        Arrays.stream(grades).forEach(g -> {
                            String name = g.getName();
                            if (name != null) {
                                gradeIds.put(name, g.getId());
                                cmbGrado.getItems().add(name);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                MostrarAlerta.advertencia("Error", "No se pudieron cargar los grados: " + e.getMessage());
            }
        }).start();
    }

    // Cargar secciones de un grado y rellenar cmbSeccion
    private void cargarSeccionesPorGrado(int gradeId) {
        new Thread(() -> {
            try {
                SectionResponse[] sections = ApiUtils.getArray("/grades/" + gradeId + "/sections", SectionResponse[].class);
                Platform.runLater(() -> {
                    sectionIds.clear();
                    cmbSeccion.getItems().clear();
                    if (sections != null) {
                        Arrays.stream(sections).forEach(s -> {
                            String name = s.getName();
                            if (name != null) {
                                sectionIds.put(name, s.getId());
                                cmbSeccion.getItems().add(name);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                MostrarAlerta.advertencia("Error", "No se pudieron cargar las secciones: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    void copiarCorreoYContraseniaDeAlumno(ActionEvent event) {
        String email = lblCorreoEstudianteNew.getText();
        String password = lblContraseniaEstudianteNew.getText();

        if ((email == null || email.isBlank()) && (password == null || password.isBlank())) {
            System.err.println("No hay correo ni contraseña para copiar.");
            return;
        }

        String texto = "Correo: " + (email == null ? "" : email) + " Contraseña: " + (password == null ? "" : password);

        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(texto);
        clipboard.setContent(content);

        System.out.println("Datos copiados al portapapeles.");
    }
}