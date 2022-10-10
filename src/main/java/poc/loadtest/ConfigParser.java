package poc.loadtest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.loadtest.mapper.RequestMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConfigParser {

    @Autowired
    private RequestMapper mapper;

    public void parse(String config, String targetScript) throws IOException {
        JSONObject configJSON = new JSONObject(config);
        if(!isConfigValid(configJSON)) {
            System.out.println("### Invalid configuration file ###");
            return;
        }

        List<String> scriptCode = mapper.createScript(configJSON);

        FileWriter writer = new FileWriter(targetScript);

        for(String line: scriptCode) {
            writer.write(line);
        }
        writer.close();
    }

    private static Boolean isConfigValid(JSONObject configJSON) {
        return configJSON.has("baseURL") && configJSON.has("options") && configJSON.has("requests");
    }
}