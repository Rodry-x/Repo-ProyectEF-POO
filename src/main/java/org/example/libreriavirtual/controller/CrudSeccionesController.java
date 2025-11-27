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
    private ComboBox<String> cmbElegirGradoParaAgregarSeccion;

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
        // cargar grados al iniciar (ahora llena ambos combo)
        cargarGrados();

        // cuando el usuario seleccione un grado en el panel de matriculación, cargar sus secciones
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
    void guardarGrado(ActionEvent event) {
        String nombreGrado = txtGrado.getText() != null ? txtGrado.getText().trim() : "";
        if (nombreGrado.isBlank()) {
            MostrarAlerta.error("Validación", "Ingrese el nombre del grado.");
            return;
        }

        new Thread(() -> {
            try {
                PostGrade body = new PostGrade(nombreGrado);
                String jsonBody = gson.toJson(body);
                Integer gradeId = ApiUtils.postAndExtractId("/grades", jsonBody);

                Platform.runLater(() -> {
                    txtGrado.clear();
                    // recargar grados en ambos combo
                    cargarGrados();
                    MostrarAlerta.info("Éxito", "Grado creado correctamente (id: " + gradeId + ").");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear el grado: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    void guardarSeccion(ActionEvent event) {
        String selectedGradeName = cmbElegirGradoParaAgregarSeccion.getValue();
        if (selectedGradeName == null || selectedGradeName.isBlank()) {
            MostrarAlerta.error("Validación", "Seleccione un grado donde agregar la sección.");
            return;
        }
        Integer gradeId = gradeIds.get(selectedGradeName);
        if (gradeId == null) {
            MostrarAlerta.error("Validación", "No se pudo resolver el id del grado seleccionado.");
            return;
        }

        String nombreSeccion = txtSeccion.getText() != null ? txtSeccion.getText().trim() : "";
        if (nombreSeccion.isBlank()) {
            MostrarAlerta.error("Validación", "Ingrese el nombre de la sección.");
            return;
        }

        new Thread(() -> {
            try {
                PostSecciones bodySeccion = new PostSecciones(nombreSeccion);
                String jsonBodySeccion = gson.toJson(bodySeccion);
                Integer sectionCreatedId = ApiUtils.postAndExtractId("/grades/" + gradeId + "/sections", jsonBodySeccion);

                Platform.runLater(() -> {
                    txtSeccion.clear();
                    // si el grado seleccionado en la parte de matriculación coincide, recargar sus secciones
                    String currentMatriculaGrade = cmbGrado.getValue();
                    if (currentMatriculaGrade != null && gradeIds.get(currentMatriculaGrade) != null
                            && gradeIds.get(currentMatriculaGrade).equals(gradeId)) {
                        cargarSeccionesPorGrado(gradeId);
                    }
                    MostrarAlerta.info("Éxito", "Sección creada correctamente (id: " + sectionCreatedId + ").");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MostrarAlerta.error("Error", "Fallo al crear la sección: " + e.getMessage()));
            }
        }).start();
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

    // Cargar todos los grados desde la API y rellenar cmbGrado y cmbElegirGradoParaAgregarSeccion
    private void cargarGrados() {
        new Thread(() -> {
            try {
                GradeResponse[] grades = ApiUtils.getArray("/grades", GradeResponse[].class);
                Platform.runLater(() -> {
                    gradeIds.clear();
                    cmbGrado.getItems().clear();
                    cmbElegirGradoParaAgregarSeccion.getItems().clear();
                    if (grades != null) {
                        Arrays.stream(grades).forEach(g -> {
                            String name = g.getName();
                            if (name != null) {
                                gradeIds.put(name, g.getId());
                                cmbGrado.getItems().add(name);
                                cmbElegirGradoParaAgregarSeccion.getItems().add(name);
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