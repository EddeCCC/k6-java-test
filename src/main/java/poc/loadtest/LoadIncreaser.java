package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LoadIncreaser {

    public String increase(String config) {
        JSONObject configJSON = new JSONObject(config);
        JSONObject options = configJSON.getJSONObject("options");
        JSONObject scenarios = options.getJSONObject("scenarios");
        JSONObject scenario = scenarios.getJSONObject("breakpoint");
        JSONArray stages = scenario.getJSONArray("stages");

        JSONArray newStages = this.multiplyTargets(stages, 2);
        JSONObject newScenario = scenario.put("stages", newStages);
        JSONObject newScenarios = scenarios.put("breakpoint", newScenario);
        JSONObject newOptions = options.put("scenarios", newScenarios);
        JSONObject newConfig = configJSON.put("options", newOptions);

        return newConfig.toString();
    }

    private JSONArray multiplyTargets(JSONArray array, int factor) {
        for(int i = 0; i < array.length(); i++) {
            JSONObject stage = array.getJSONObject(i);
            int currentLoad = stage.getInt("target");
            int newLoad = currentLoad * factor;

            JSONObject newStage = stage.put("target", newLoad);
            array.put(i, newStage);
        }
        return array;
    }
}