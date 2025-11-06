// java
package org.example.libreriavirtual.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;
import org.example.libreriavirtual.model.User;
import org.example.libreriavirtual.service.ApiClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SesionController {
    private static final Map<Integer, User> activeUsers = new ConcurrentHashMap<>();
    private static final Path SESSION_FILE = Paths.get(System.getProperty("user.home"), ".libreria_session.json");
    private static final Gson gson = new Gson();

    // Ids actualmente logueadas (persistidas en disco)
    private static volatile Integer currentProfesorId = null;
    private static volatile Integer currentEstudianteId = null;

    // Iniciar/registrar sesión (añade al mapa y marca según rol)
    public static void iniciarSesion(User user) {
        if (user == null) return;
        activeUsers.put(user.getId(), user);

        if (user.getRole() != null) {
            if (user.getRole().equalsIgnoreCase("profesor")) {
                currentProfesorId = user.getId();
                persistirSesion();
            } else if (user.getRole().equalsIgnoreCase("estudiante")) {
                currentEstudianteId = user.getId();
                persistirSesion();
            }
        }
    }

    // Cerrar sesión del profesor actual
    public static void cerrarSesionProfesor() {
        if (currentProfesorId != null) {
            activeUsers.remove(currentProfesorId);
            currentProfesorId = null;
            persistirSesion();
        }
    }

    // Cerrar sesión del estudiante actual
    public static void cerrarSesionEstudiante() {
        if (currentEstudianteId != null) {
            activeUsers.remove(currentEstudianteId);
            currentEstudianteId = null;
            persistirSesion();
        }
    }

    // Obtener ids activas (pueden ser null)
    public static Integer getProfesorIdActivo() {
        return currentProfesorId;
    }

    public static Integer getEstudianteIdActivo() {
        return currentEstudianteId;
    }

    // Obtener usuario activo por id (o null)
    public static User getUsuarioActivo(int id) {
        return activeUsers.get(id);
    }

    // Resto de utilidades (listas por rol)
    public static List<User> getEstudiantesActivos() {
        return activeUsers.values()
                .stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("estudiante"))
                .collect(Collectors.toList());
    }

    public static List<User> getProfesoresActivos() {
        return activeUsers.values()
                .stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("profesor"))
                .collect(Collectors.toList());
    }

    public static List<User> getTodosActivos() {
        return activeUsers.values().stream().collect(Collectors.toList());
    }

    // Persistir las ids a disco
    private static void persistirSesion() {
        try {
            String json = gson.toJson(Map.of(
                    "currentProfesorId", currentProfesorId,
                    "currentEstudianteId", currentEstudianteId
            ));
            Files.writeString(SESSION_FILE, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Borrar archivo de sesión
    private static void borrarSesionPersistida() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cargar sesión persistida (llamar al inicio de la aplicación)
    public static void cargarSesionPersistida() {
        if (Files.exists(SESSION_FILE)) {
            try {
                String json = Files.readString(SESSION_FILE, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, Integer>>() {}.getType();
                Map<String, Integer> m = gson.fromJson(json, type);
                Integer pid = m != null ? m.get("currentProfesorId") : null;
                Integer epid = m != null ? m.get("currentEstudianteId") : null;

                if (pid != null) {
                    currentProfesorId = pid;
                    sincronizarProfesorDesdeApi(pid);
                }
                if (epid != null) {
                    currentEstudianteId = epid;
                    sincronizarEstudianteDesdeApi(epid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Sincroniza los datos del profesor guardado consultando /users y buscando por id
    private static void sincronizarProfesorDesdeApi(int id) {
        try (Response response = ApiClient.request("/users", "GET", null)) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> usuarios = gson.fromJson(json, listType);
                usuarios.stream()
                        .filter(u -> u.getId() == id)
                        .findFirst()
                        .ifPresent(u -> activeUsers.put(id, u));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sincroniza los datos del estudiante guardado consultando /users y buscando por id
    private static void sincronizarEstudianteDesdeApi(int id) {
        try (Response response = ApiClient.request("/users", "GET", null)) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> usuarios = gson.fromJson(json, listType);
                usuarios.stream()
                        .filter(u -> u.getId() == id)
                        .findFirst()
                        .ifPresent(u -> activeUsers.put(id, u));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}