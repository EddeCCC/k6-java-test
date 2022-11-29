package poc.loadtest;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;

@Component
public class ConfigLoader {

    public String loadConfig() throws URISyntaxException, IOException, InterruptedException {
        String serverURI = "http://127.0.0.1:8080/config";
        URI uri = new URI(serverURI);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if(statusCode != 200) throw new RuntimeException("Loading config failed - Status not 200, but " + statusCode);
        String config = response.body();
        return config;
    }
}