package org.example.libreriavirtual.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.libreriavirtual.model.*;
import org.example.libreriavirtual.service.ApiClient;
import org.example.libreriavirtual.utilities.LimpiarCasillasController;
import org.example.libreriavirtual.utilities.SceneController;
import org.example.libreriavirtual.utilities.Path;
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
            System.err.println("Seleccione una sección antes de matricular al estudiante.");
            return;
        }
        Integer sectionId = sectionIds.get(selectedSectionName);
        if (sectionId == null) {
            System.err.println("No se pudo resolver el id de la sección seleccionada.");
            return;
        }
        //Generar email y contraseña automáticamente
        // leer nombre y apellidos
        String nombre = txtNombreEstudiante.getText() != null ? txtNombreEstudiante.getText().trim() : "";
        String apellidos = txtApellidosEstudiante.getText() != null ? txtApellidosEstudiante.getText().trim() : "";
        String fullName = (nombre + " " + apellidos).trim();

        // generar email y contraseña aquí mismo
        String email = buildEmailFromName(nombre, apellidos);
        String password = generateRandomPassword(10); // asegura > 8 caracteres

        // actualizar labels en la UI
        lblCorreoEstudianteNew.setText(email);
        lblContraseniaEstudianteNew.setText(password);

        PostStudent postStudent = new PostStudent(fullName, email, "student", password);
        String jsonBody = gson.toJson(postStudent);

        try (var response = ApiClient.request("/users", "POST", jsonBody)) {
            String responseBody = response.body().string();
            System.out.println("User Response code: " + response.code());
            System.out.println("User Response body: " + responseBody);

            Integer studentId = null;

            // intentar obtener id desde el body (JSON)
            if (response.isSuccessful() && responseBody != null && !responseBody.isBlank()) {
                try {
                    StudentResponse created = gson.fromJson(responseBody, StudentResponse.class);
                    studentId = created != null ? created.getId() : null;
                } catch (Exception ignored) {
                }
            }

            // fallback a header Location si no hay body con id
            if (studentId == null) {
                String loc = response.header("Location");
                if (loc != null && !loc.isBlank()) {
                    String[] parts = loc.split("/");
                    String last = parts[parts.length - 1];
                    try {
                        studentId = Integer.valueOf(last);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (studentId == null) {
                System.err.println("No se pudo obtener el id del estudiante creado. Revisar la respuesta del servidor.");
                return;
            }

            // matricular estudiante en la sección
            Map<String, Integer> enrollBody = Map.of("student_id", studentId);
            String enrollJson = gson.toJson(enrollBody);
            String enrollPath = "/sections/" + sectionId + "/students";

            try (var resp2 = ApiClient.request(enrollPath, "POST", enrollJson)) {
                String enrollRespBody = resp2.body().string();
                System.out.println("Enroll Response code: " + resp2.code());
                System.out.println("Enroll Response body: " + enrollRespBody);

                // actualizar UI: recargar secciones del grado seleccionado (opcional)
                String selectedGrade = cmbGrado.getValue();
                if (selectedGrade != null) {
                    Integer gradeId = gradeIds.get(selectedGrade);
                    if (gradeId != null) {
                        cargarSeccionesPorGrado(gradeId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void enviarLosDatosSeccion(ActionEvent event) {
        PostGrade body = new PostGrade(txtGrado.getText());
        String jsonBody = gson.toJson(body);

        try (var response = ApiClient.request("/grades", "POST", jsonBody)) {
            String responseBody = response.body().string();

            System.out.println("Response code: " + response.code());
            System.out.println("Response body: " + responseBody);

            Integer gradeId = null;

            // Intentar leer id desde el body (JSON) si el backend lo devuelve
            if (response.isSuccessful() && responseBody != null && !responseBody.isBlank()) {
                try {
                    GradeResponse created = gson.fromJson(responseBody, GradeResponse.class);
                    gradeId = created != null ? created.getId() : null;
                } catch (Exception e) {
                    // ignore parsing error y fallback a Location header
                }
            }

            // Si no hay body con id, intentar leer header Location
            if (gradeId == null) {
                String loc = response.header("Location");
                if (loc != null && !loc.isBlank()) {
                    String[] parts = loc.split("/");
                    String last = parts[parts.length - 1];
                    try {
                        gradeId = Integer.valueOf(last);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (gradeId == null) {
                System.err.println("No se pudo obtener el id del grado creado. Revisar la respuesta del servidor.");
                return;
            }

            // Ahora crear la sección usando el id obtenido
            PostSecciones bodySeccion = new PostSecciones(txtSeccion.getText());
            String jsonBodySeccion = gson.toJson(bodySeccion);

            String path = "/grades/" + gradeId + "/sections"; // ajustar si la API usa otra ruta
            try (var resp2 = ApiClient.request(path, "POST", jsonBodySeccion)) {
                String responseBody2 = resp2.body().string();
                System.out.println("Section Response code: " + resp2.code());
                System.out.println("Section Response body: " + responseBody2);
                // Para actualizar la vista, recargar la escena de secciones
                sceneController.cambiarEscena(event, Path.PANEL_SECCIONES_FXML);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            try (var response = ApiClient.request("/grades", "GET", null)) {
                if (!response.isSuccessful()) return;
                String body = response.body().string();
                GradeResponse[] grades = gson.fromJson(body, GradeResponse[].class);
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
            }
        }).start();
    }

    // Cargar secciones de un grado y rellenar cmbSeccion
    private void cargarSeccionesPorGrado(int gradeId) {
        new Thread(() -> {
            try (var response = ApiClient.request("/grades/" + gradeId + "/sections", "GET", null)) {
                if (!response.isSuccessful()) return;
                String body = response.body().string();
                SectionResponse[] sections = gson.fromJson(body, SectionResponse[].class);
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
            }
        }).start();
    }

    // Construye un email a partir de nombre y apellidos.
    // Normaliza a minúsculas, espacios por puntos y elimina caracteres inválidos.
    private String buildEmailFromName(String nombre, String apellidos) {
        String combined = (nombre + " " + apellidos).trim();
        if (combined.isEmpty()) {
            // fallback si no hay datos
            return "user" + System.currentTimeMillis() % 10000 + "@libreria.pe";
        }
        String sanitized = combined.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\s+", ".")           // espacios -> puntos
                .replaceAll("[^a-z0-9.]", "");    // quitar caracteres no alfanum ni punto
        // evitar empezar o terminar con punto
        sanitized = sanitized.replaceAll("^\\.+|\\.+$", "");
        if (sanitized.isEmpty()) {
            return "user" + System.currentTimeMillis() % 10000 + "@libreria.pe";
        }
        return sanitized + "@libreria.pe";
    }

    // Genera una contraseña aleatoria con longitud mínima > 8.
    // Usa SecureRandom y un conjunto seguro de caracteres.
    private String generateRandomPassword(int length) {
        int minLength = Math.max(9, length);
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String digits = "0123456789";
        final String symbols = "!@#$%&*()-_=+";
        final String all = upper + lower + digits + symbols;

        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(minLength);

        // asegurar que haya al menos una mayúscula, una minúscula y un dígito
        sb.append(upper.charAt(rnd.nextInt(upper.length())));
        sb.append(lower.charAt(rnd.nextInt(lower.length())));
        sb.append(digits.charAt(rnd.nextInt(digits.length())));

        for (int i = 3; i < minLength; i++) {
            sb.append(all.charAt(rnd.nextInt(all.length())));
        }

        // barajar los caracteres para no colocar las obligatorias al inicio
        char[] pwd = sb.toString().toCharArray();
        for (int i = pwd.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = pwd[i];
            pwd[i] = pwd[j];
            pwd[j] = tmp;
        }
        return new String(pwd);
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