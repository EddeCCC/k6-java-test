package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ParamsMapper implements K6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject params = request.getJSONObject("params");
        String paramsString = params.toString();

        return String.format("%svar params%d = %s%s",
                newLine, requestIndex, paramsString, newLine);
    }
}