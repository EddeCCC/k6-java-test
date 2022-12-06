package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class PayloadMapper implements K6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject payload = request.getJSONObject("payload");
        String payloadString = payload.toString();

        return String.format("%svar payload%d = %s%s",
                newLine, requestIndex, payloadString, newLine);
    }
}