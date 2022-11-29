package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * LoadIncreaser copies the first stage and inserts it at the start of the JSONArray
 * Then all stages (except the copied one) will be multiplied by a specific factor
 * The copying ensures that the load will not increase too drastically
 */
@Component
public class LoadIncreaser {

    public String increaseLoad(String config) {
        JSONObject configJSON = new JSONObject(config);
        JSONArray stages = configJSON
                .getJSONObject("options")
                .getJSONObject("scenarios")
                .getJSONObject("breakpoint")
                .getJSONArray("stages");

        this.multiplyTargets(stages, 2);

        return configJSON.toString();
    }

    private JSONArray multiplyTargets(JSONArray array, int factor) {
        //Iterate from end to the start
        for(int i = array.length(); i > 0; i--) {
            JSONObject stage = array.getJSONObject(i-1);
            int currentLoad = stage.getInt("target");
            int newLoad = currentLoad * factor;

            JSONObject newStage = new JSONObject(stage.toMap());
            newStage.put("target", newLoad);
            array.put(i, newStage);
        }
        return array;
    }
}