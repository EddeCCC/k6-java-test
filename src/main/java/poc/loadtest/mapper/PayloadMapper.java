package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
class PayloadMapper implements k6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject payload = request.getJSONObject("payload");
        String payloadString = payload.toString();

        return String.format("%svar payload%d = %s%s",
                newLine, requestIndex, payloadString, newLine);
    }
}