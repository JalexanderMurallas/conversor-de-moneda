import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Properties;

public class ConsultaDivisa {

    public String consultaDivisa(){
        String keyAPI = getAPIKey();
        URI direccion = URI.create("https://v6.exchangerate-api.com/v6/"+ keyAPI + "/latest/" +Principal.divisaInicial);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.valueOf(direccion)))
                .build();

        try {
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            //return new Gson().fromJson(response.body(), String.class);
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("No encontrada");
        }
    }

    private String getAPIKey() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            return properties.getProperty("API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
