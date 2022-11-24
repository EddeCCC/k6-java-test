package poc.grafana;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Send Dashboards to the Grafana API via Basic Authentication
 * The Authorization header has to be Base64 encoded
 */
@Component
public class GrafanaClient {

    private final String postDashboardURI = "http://localhost:3030/api/dashboards/db";
    private final String encodedBasicAuthorization = "YWRtaW46YWRtaW4=";

    public String sendDashboard(String file) throws URISyntaxException, IOException, InterruptedException {

        URI uri = new URI(postDashboardURI);
        String dashboard = this.getDashboard(file);
        BodyPublisher bodyPublisher = BodyPublishers.ofString(dashboard);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + encodedBasicAuthorization)
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if(statusCode != 200) throw new RuntimeException("Sending Dashboard failed - Status not 200, but " + statusCode);
        return response.body();
    }

    private String getDashboard(String file) throws IOException {
        String text = this.getFileText(file);
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