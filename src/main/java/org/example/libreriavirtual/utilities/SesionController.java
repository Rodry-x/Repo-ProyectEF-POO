package org.example.libreriavirtual.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;
import org.example.libreriavirtual.model.User;
import org.example.libreriavirtual.service.ApiClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SesionController {
    private static final java.util.Map<Integer, User> activeUsers = new ConcurrentHashMap<>();
    private static final java.nio.file.Path SESSION_FILE = java.nio.file.Paths.get(System.getProperty("user.home"), ".libreria_session.json");
    private static final Gson gson = new Gson();

    private static volatile Integer currentProfesorId = null;
    private static volatile Integer currentEstudianteId = null;

    public static void iniciarSesion(User user) {
        if (user == null) return;
        int id = user.getId();
        if (id <= 0) return;

        activeUsers.put(id, user);

        String role = user.getRole();
        if (role != null) {
            String r = role.trim().toLowerCase();
            if (r.equals("profesor") || r.equals("teacher")) {
                currentProfesorId = id;
            } else if (r.equals("estudiante") || r.equals("student")) {
                currentEstudianteId = id;
            }
        }

        boolean ok = persistirSesion();
        System.out.println("[SesionController] iniciarSesion -> id: " + id + ", role: " + role + ", persistirOk: " + ok);
    }

    public static void cerrarSesionProfesor() {
        if (currentProfesorId != null) {
            activeUsers.remove(currentProfesorId);
            currentProfesorId = null;
            persistirSesion();
        }
    }

    // Nuevo: cerrar sesión del estudiante activo
    public static void cerrarSesionEstudiante() {
        if (currentEstudianteId != null) {
            activeUsers.remove(currentEstudianteId);
            currentEstudianteId = null;
            persistirSesion();
        }
    }

    public static Integer getProfesorIdActivo() {
        return currentProfesorId;
    }

    public static User getUsuarioActivo(Integer id) {
        if (id == null) return null;
        return activeUsers.get(id);
    }

    public static User getProfesorActivo() {
        return getUsuarioActivo(currentProfesorId);
    }

    // Nuevo: obtener el usuario estudiante actualmente activo
    public static User getEstudianteActivo() {
        return getUsuarioActivo(currentEstudianteId);
    }

    public static boolean persistirSesion() {
        try {
            java.nio.file.Path parent = SESSION_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            java.util.Map<String, Integer> data = new java.util.HashMap<>();
            data.put("currentProfesorId", currentProfesorId);
            data.put("currentEstudianteId", currentEstudianteId);

            String json = gson.toJson(data);
            Files.writeString(SESSION_FILE, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            boolean exists = Files.exists(SESSION_FILE);
            System.out.println("[SesionController] Sesión persistida en: " + SESSION_FILE + " -> " + json + " (exists=" + exists + ")");
            return exists;
        } catch (Exception e) {
            System.err.println("[SesionController] Error al persistir sesión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void borrarSesionPersistida() {
        try {
            Files.deleteIfExists(SESSION_FILE);
            System.out.println("[SesionController] Archivo de sesión borrado: " + SESSION_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cargarSesionPersistida() {
        try {
            if (!Files.exists(SESSION_FILE)) {
                System.out.println("[SesionController] No existe archivo de sesión en: " + SESSION_FILE);
                return;
            }
            String json = Files.readString(SESSION_FILE, StandardCharsets.UTF_8);
            System.out.println("[SesionController] Leyendo sesión: " + json);
            Type type = new TypeToken<java.util.Map<String, Integer>>() {}.getType();
            java.util.Map<String, Integer> m = gson.fromJson(json, type);
            Integer pid = m != null ? m.get("currentProfesorId") : null;
            Integer epid = m != null ? m.get("currentEstudianteId") : null;
            if (pid != null) {
                currentProfesorId = pid;
                sincronizarProfesor(pid);
            }
            if (epid != null) {
                currentEstudianteId = epid;
                sincronizarEstudiante(epid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sincronizarProfesor(int id) {
        try (Response response = ApiClient.request("/users", "GET", null)) {
            if (response != null && response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> usuarios = gson.fromJson(json, listType);
                usuarios.stream()
                        .filter(u -> Objects.equals(u.getId(), id))
                        .findFirst()
                        .ifPresent(u -> {
                            activeUsers.put(id, u);
                            System.out.println("[SesionController] Profesor sincronizado desde API: " + u.getEmail());
                        });
            } else {
                System.out.println("[SesionController] Falló request /users al sincronizar profesor (response null o no exitoso).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sincronizarEstudiante(int id) {
        try (Response response = ApiClient.request("/users", "GET", null)) {
            if (response != null && response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> usuarios = gson.fromJson(json, listType);
                usuarios.stream()
                        .filter(u -> Objects.equals(u.getId(), id))
                        .findFirst()
                        .ifPresent(u -> activeUsers.put(id, u));
            } else {
                System.out.println("[SesionController] Falló request /users al sincronizar estudiante.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static java.nio.file.Path getSessionFilePath() {
        return SESSION_FILE;
    }

    public static boolean sesionPersistidaExiste() {
        try {
            return Files.exists(SESSION_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer getEstudianteIdActivo() {
        return currentEstudianteId;
    }

    // Devuelve el usuario actualmente activo (prioriza estudiante, luego profesor)
    public static User getUsuarioActual() {
        if (currentEstudianteId != null) return getUsuarioActivo(currentEstudianteId);
        if (currentProfesorId != null) return getUsuarioActivo(currentProfesorId);
        return null;
    }

    // Intenta extraer un entero representando el id de grado del usuario activo
    public static Integer getGradeIdActivo() {
        User u = getUsuarioActual();
        return extractIntFromUser(u,
                new String[]{"getGradeId", "getGrade_id", "getGrade", "getGradeIdCached", "getGradeIdValue"},
                new String[]{"gradeId", "grade_id", "grade"});
    }

    // Intenta extraer un entero representando el id de sección del usuario activo
    public static Integer getSectionIdActivo() {
        User u = getUsuarioActual();
        return extractIntFromUser(u,
                new String[]{"getSectionId", "getSection_id", "getSection", "getSectionIdCached", "getSectionIdValue"},
                new String[]{"sectionId", "section_id", "section"});
    }

    // Helper que usa reflexión para soportar distintas convenciones de nombres en User
    private static Integer extractIntFromUser(User u, String[] methodNames, String[] fieldNames) {
        if (u == null) return null;
        try {
            Class<?> cls = u.getClass();
            for (String mName : methodNames) {
                try {
                    Method m = cls.getMethod(mName);
                    Object val = m.invoke(u);
                    Integer parsed = convertToInteger(val);
                    if (parsed != null) return parsed;
                } catch (NoSuchMethodException ignored) {
                }
            }
            for (String fName : fieldNames) {
                try {
                    Field f = cls.getDeclaredField(fName);
                    f.setAccessible(true);
                    Object val = f.get(u);
                    Integer parsed = convertToInteger(val);
                    if (parsed != null) return parsed;
                } catch (NoSuchFieldException ignored) {
                }
            }
        } catch (Exception ex) {
            // no hacer nada, devolver null
        }
        return null;
    }

    private static Integer convertToInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt(((String) val).trim()); } catch (Exception ignored) {}
        }
        return null;
    }
}