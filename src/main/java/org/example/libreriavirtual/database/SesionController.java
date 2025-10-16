package org.example.libreriavirtual.database;

public class SesionController {
    // Variables para almacenar los IDs de las sesiones activas
    private static int idProfesor = -1;
    private static int idEstudiante = -1;

    public static void setIdProfesor(int id) {
        idProfesor = id;
    }

    public static int getIdProfesor() {
        return idProfesor;
    }

    public static void cerrarSesionProfesor() {
        idProfesor = -1;
    }

    public static void setIdEstudiante(int id) {
        idEstudiante = id;
    }

    public static int getIdEstudiante() {
        return idEstudiante;
    }

    public static void cerrarSesionEstudiante() {
        idEstudiante = -1;
    }

    public static void cerrarTodasLasSesiones() {
        idProfesor = -1;
        idEstudiante = -1;
    }
}