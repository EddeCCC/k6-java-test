package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConfigParser {

    private static final JSONScriptMapper mapper = new JSONScriptMapper();

    public static void parse(String configUrl, String targetScript) throws IOException {
        String configText = Files.readString(Paths.get(configUrl));

        JSONObject json = new JSONObject(configText);
        JSONArray array = json.getJSONArray("params");

        FileWriter writer = new FileWriter(targetScript);

        for(Object line: array) {
            writer.write(line.toString());
        }
        writer.close();
    }

    public static void newParse(String configURL, String targetScript) throws IOException {
        String configText = Files.readString(Paths.get(configURL));
        JSONObject json = new JSONObject(configText);
        JSONObject request = json.getJSONObject("request");
        List<String> scriptText = mapper.mapConfig(request);

        FileWriter writer = new FileWriter(targetScript);

        for(String line: scriptText) {
            writer.write(line);
        }
        writer.close();
    }
}