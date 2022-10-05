package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ChecksMapper implements k6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject checks = request.getJSONObject("checks");
        String type = request.getString("type");
        StringBuilder checkBuilder = new StringBuilder();

        if(checks.has("status")) {
            Integer status = Integer.parseInt( checks.getString("status") );
            String statusScript;
            if(checks.has("OR-status")) {
                Integer orStatus = Integer.parseInt( checks.getString("OR-status") );
                statusScript = String.format("\t'%s status was %s/%s': x => x.status && (x.status == %s || x.status == %s),%s",
                        type, status, orStatus, status, orStatus, newLine);
            }
            else {
                statusScript = String.format("\t'%s status was %s': x => x.status && x.status == %s,%s",
                        type, status, status, newLine);
            }

            checkBuilder.append(statusScript);
        }
        if(checks.has("body")) {
            JSONObject body = checks.getJSONObject("body");

            if(body.has("min-length")) {
                Integer minLength = Integer.parseInt( body.getString("min-length") ) - 1;
                String minLengthScript = String.format("\t'%s body size > %d': x => x.body && x.body.length > %d,%s",
                        type, minLength, minLength, newLine);
                checkBuilder.append(minLengthScript);
            }
            //More checks with body...
        }

        return String.format("check(response%d, {%s%s});%s",
                requestIndex, newLine, checkBuilder, newLine);
    }
}