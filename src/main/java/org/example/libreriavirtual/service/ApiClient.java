package org.example.libreriavirtual.service;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final OkHttpClient apiClient = new OkHttpClient();
    private static final String BASE_URL = "https://upn-libreria.duckdns.org";

    public static Response request(String endpoint, String method, String jsonBody) throws Exception{
        RequestBody body = null;
        if(jsonBody != null){
            body = RequestBody.create(jsonBody.getBytes());
        }

        Request.Builder builder = new Request.Builder();
        builder.url(BASE_URL + endpoint);

        switch(method.toUpperCase()){
            case "POST":
                builder.post(body);
                break;
            case "PUT":
                builder.put(body);
                break;
            case "DELETE":
                builder.delete(body);
                break;
            case "GET":
            default:
                builder.get();
                break;
        }
        return apiClient.newCall(builder.build()).execute();
    }
}
