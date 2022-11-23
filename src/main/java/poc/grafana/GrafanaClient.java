package poc.grafana;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class GrafanaClient {

    private final String tokenBasedAuthURI = "http://127.0.0.1:3030/api/dashboards/db";
    private final String APIKey = "eyJrIjoiNkpqRnppSUpKdUN0YTR5TmY5MENJd0hIUkx6blFyMWciLCJuIjoiamF2YSIsImlkIjoxfQ==";

    public String sendDashboard() throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI(tokenBasedAuthURI);
        String dashboard = this.getDashboard("home.json");
        BodyPublisher bodyPublisher = BodyPublishers.ofString(dashboard);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + APIKey)
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if(statusCode != 200) throw new RuntimeException("Sending Dashboard failed - Status not 200, but " + statusCode);
        String config = response.body();
        return config;
    }

    private String getDashboard(String filename) throws IOException {
        String text = this.getFileText(filename);
        JSONObject json = new JSONObject(text);

        JSONObject dashboard = new JSONObject();
        dashboard.put("dashboard", json);
        dashboard.put("overwrite", true);

        return dashboard.toString();
    }

    private String getFileText(String filename) throws IOException {
        String projectDirectory = System.getProperty("user.dir");
        String filePath = projectDirectory + "/docker-config/grafana/my-dashboards/" + filename;
        Path path = Paths.get(filePath);

        return new String(Files.readAllBytes(path));
    }
}