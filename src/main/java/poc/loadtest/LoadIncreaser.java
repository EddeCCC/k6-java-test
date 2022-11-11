package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LoadIncreaser {

    public JSONObject increase(JSONObject config) {
        JSONObject options = config.getJSONObject("options");
        JSONArray stages = options.getJSONArray("stages");
        JSONObject spikeLoad = stages.getJSONObject(1);
        int currentLoad = spikeLoad.getInt("target");
        int increasedLoad = currentLoad * 2;
        System.out.println("### CURRENT LOAD: " + increasedLoad + " ###");

        JSONObject newSpikeLoad = spikeLoad.put("target", increasedLoad);
        JSONArray newStages = stages.put(1, newSpikeLoad);
        JSONObject newOptions = options.put("stages", newStages);
        JSONObject newConfig = config.put("options", newOptions);
        return newConfig;
    }
}