package poc.loadtest;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ConfigLoader {

    public String loadConfig(String address) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI(address);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() != 200) throw new RuntimeException("Loading config faild - Status not 200");
        String config = response.body();
        return config;
    }
}