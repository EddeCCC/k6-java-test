package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LoadIncreaser {

    public String increase(String config) {
        JSONObject configJSON = new JSONObject(config);
        JSONObject options = configJSON.getJSONObject("options");
        JSONArray stages = options.getJSONArray("stages");
        JSONObject spikeLoad = stages.getJSONObject(1);
        int currentLoad = spikeLoad.getInt("target");
        int increasedLoad = currentLoad * 2;
        System.out.println("### INCREASED LOAD: " + increasedLoad + " ###");

        JSONObject newCapacityLoad = spikeLoad.put("target", increasedLoad);
        JSONArray newStages = stages.put(1, newCapacityLoad);
        JSONObject newOptions = options.put("stages", newStages);
        JSONObject newConfig = configJSON.put("options", newOptions);
        return newConfig.toString();
    }
}