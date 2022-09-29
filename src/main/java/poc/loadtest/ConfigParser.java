package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class ConfigParser {

    public static void convert(String configUrl, String targetScript) throws IOException {
        String configText = Files.readString(Paths.get(configUrl));

        JSONObject json = new JSONObject(configText);
        JSONArray array = json.getJSONArray("params");

        FileWriter writer = new FileWriter(targetScript);

        for (int i = 0; i < array.length(); i++) {
            String line = array.getString(i);
            writer.write(line);
        }
        writer.close();
    }
}