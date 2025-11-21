package org.example.libreriavirtual.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.libreriavirtual.service.ApiClient;

import java.io.IOException;

public final class ApiUtils {
    private static final Gson gson = new Gson();

    private ApiUtils() { /* util */ }

    // Obtiene un arreglo JSON y lo parsea a un array de objetos T
    public static <T> T[] getArray(String path, Class<T[]> clazz) throws IOException {
        try {
            var resp = ApiClient.request(path, "GET", null);
            try (resp) {
                if (!resp.isSuccessful()) {
                    throw new IOException("HTTP " + resp.code() + " al solicitar " + path);
                }
                String body = resp.body() != null ? resp.body().string() : null;
                return gson.fromJson(body, clazz);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // Obtiene directamente un JsonArray (útil cuando no hay POJO)
    public static JsonArray getJsonArray(String path) throws IOException {
        try {
            var resp = ApiClient.request(path, "GET", null);
            try (resp) {
                if (!resp.isSuccessful()) {
                    throw new IOException("HTTP " + resp.code() + " al solicitar " + path);
                }
                String body = resp.body() != null ? resp.body().string() : null;
                return gson.fromJson(body, JsonArray.class);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // Hace POST y trata de extraer un id del body JSON (campo "id") o del header Location
    // Devuelve el id o lanza IOException si no puede obtenerlo.
    public static Integer postAndExtractId(String path, String jsonBody) throws IOException {
        try {
            var resp = ApiClient.request(path, "POST", jsonBody);
            try (resp) {
                String body = resp.body() != null ? resp.body().string() : null;

                if (body != null && !body.isBlank()) {
                    try {
                        JsonObject obj = gson.fromJson(body, JsonObject.class);
                        if (obj != null && obj.has("id") && !obj.get("id").isJsonNull()) {
                            return obj.get("id").getAsInt();
                        }
                    } catch (Exception ignored) { /* no es JSON con id; seguir a Location */ }
                }

                String loc = resp.header("Location");
                if (loc != null && !loc.isBlank()) {
                    String[] parts = loc.split("/");
                    String last = parts[parts.length - 1];
                    try {
                        return Integer.valueOf(last);
                    } catch (NumberFormatException ignored) { /* no numérico */ }
                }

                throw new IOException("No se pudo extraer id desde POST " + path + " (HTTP " + resp.code() + ")");
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
