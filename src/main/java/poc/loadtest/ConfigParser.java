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

    private static final JSONScriptMapper mapper = new JSONScriptMapper("/config/options.json");

    public static void newParse(String configURL, String targetScript) throws IOException {
        String configText = Files.readString(Paths.get(configURL));
        JSONObject json = new JSONObject(configText);
        JSONArray requests = json.getJSONArray("requests");
        List<String> scriptText = mapper.createScript(requests);

        FileWriter writer = new FileWriter(targetScript);

        for(String line: scriptText) {
            writer.write(line);
        }
        writer.close();
    }
}