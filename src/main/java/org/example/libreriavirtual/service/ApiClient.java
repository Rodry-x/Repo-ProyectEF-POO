package org.example.libreriavirtual.service;
import okhttp3.*;

public class ApiClient {
    private static final OkHttpClient apiClient = new OkHttpClient();
    private static final String BASE_URL = "https://upn-libreria.duckdns.org";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Response request(String endpoint, String method, String jsonBody) throws Exception {
        RequestBody body = null;
        if (jsonBody != null) {
            body = RequestBody.create(jsonBody, JSON);
        }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint);

        switch (method.toUpperCase()) {
            case "POST":
                builder.post(body != null ? body : RequestBody.create("", JSON));
                break;
            case "PATCH":
                builder.patch(body != null ? body : RequestBody.create("", JSON));
                break;
            case "DELETE":
                if (body != null) builder.delete(body);
                else builder.delete();
                break;
            case "GET":
            default:
                builder.get();
                break;
        }

        return apiClient.newCall(builder.build()).execute();
    }
}
