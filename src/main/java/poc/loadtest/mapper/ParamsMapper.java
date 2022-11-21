package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParamsMapper implements k6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject params = request.getJSONObject("params");
        String payloadString = params.toString();

        return String.format("%svar params%d = %s%s",
                newLine, requestIndex, payloadString, newLine);
    }
}