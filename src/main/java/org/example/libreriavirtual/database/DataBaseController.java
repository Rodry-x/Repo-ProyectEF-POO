package org.example.libreriavirtual.database;

import java.sql.*;

public class DataBaseController {

    private static final String URL = "jdbc:sqlite:src/main/java/org/example/libreriavirtual/database/libreriaDB.sqlite";

    public enum ResultadoLogin {
        EXITO,
        CONTRASENIA_INCORRECTA,
        EMAIL_NO_EXISTE,
        ERROR_BD
    }

    public int obtenerIdProfesorPorEmail(String email) {
        // Consulta SQL para obtener el ID del profesor por su email
        String query = "SELECT id_profesor FROM profesor WHERE email = ?";

        //Conexión a la base de datos y ejecución de la consulta
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_profesor");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si no se encuentra el profesor, devolver -1
        return -1;
    }

    public String obtenerEmailProfesorPorId(int idProfesor) {
        // Consulta SQL para obtener el email del profesor por su ID
        String query = "SELECT email FROM profesor WHERE id_profesor = ?";

        //Conexión a la base de datos y ejecución de la consulta
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idProfesor);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si no se encuentra el profesor, devolver una cadena vacía
        return "";
    }

    public boolean actualizarEmailProfesor(int idProfesor, String nuevoEmail) {
        // Consulta SQL para actualizar el email del profesor
        String query = "UPDATE profesor SET email = ? WHERE id_profesor = ?";

        //Conexión a la base de datos y ejecución de la actualización
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevoEmail);
            pstmt.setInt(2, idProfesor);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarContraseniaProfesor(int idProfesor, String nuevaContrasenia) {
        // Consulta SQL para actualizar la contraseña del profesor
        String query = "UPDATE profesor SET contrasenia = ? WHERE id_profesor = ?";

        //Conexión a la base de datos y ejecución de la actualización
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuevaContrasenia);
            pstmt.setInt(2, idProfesor);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public ResultadoLogin validarLoginProfesor(String email, String contrasenia) {
        // Consulta SQL para obtener la contraseña del profesor por su email
        String query = "SELECT id_profesor, contrasenia FROM profesor WHERE email = ?";

        //Conexión a la base de datos y ejecución de la consulta
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            // Verificar si se encontró el email
            if (rs.next()) {
                String contraseniaBD = rs.getString("contrasenia");
                if (contraseniaBD.equals(contrasenia)) {
                    return ResultadoLogin.EXITO;
                } else {
                    return ResultadoLogin.CONTRASENIA_INCORRECTA;
                }
            } else {
                return ResultadoLogin.EMAIL_NO_EXISTE;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoLogin.ERROR_BD;
        }
    }
}