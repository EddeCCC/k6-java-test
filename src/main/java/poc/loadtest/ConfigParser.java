package poc.loadtest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.InvalidConfigurationException;
import poc.loadtest.mapper.RequestMapper;

import java.io.*;
import java.util.List;

@Component
public class ConfigParser {

    @Autowired
    private RequestMapper mapper;

    public void parse(String config, String scriptPath) throws IOException {
        JSONObject configJSON = new JSONObject(config);
        if(!isConfigValid(configJSON)) throw new InvalidConfigurationException();

        List<String> scriptCode = mapper.createScript(configJSON);
        FileWriter writer = new FileWriter(scriptPath);

        for(String line: scriptCode) writer.write(line);
        writer.close();
    }

    private Boolean isConfigValid(JSONObject config) {
        return config.has("baseURL") && config.has("options") && config.has("requests");
    }
}