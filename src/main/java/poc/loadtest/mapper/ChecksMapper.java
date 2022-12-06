package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ChecksMapper implements K6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject checks = request.getJSONObject("checks");
        String type = request.getString("type");
        StringBuilder checkBuilder = new StringBuilder();

        if(checks.has("status")) {
            int status = checks.getInt("status");
            String statusScript;
            if(checks.has("OR-status")) {
                int orStatus = checks.getInt("OR-status");
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
                int minLength = body.getInt("min-length");
                String minLengthScript = String.format("\t'%s body size >= %d': x => x.body && x.body.length >= %d,%s",
                        type, minLength, minLength, newLine);
                checkBuilder.append(minLengthScript);
            }
            if(body.has("includes")) {
                String includes = body.getString("includes");
                String includesScript = String.format("\t'body includes %s': x => x.body && x.body.includes('%s'),%s",
                        includes, includes, newLine);
                checkBuilder.append(includesScript);
            }
        }
        if(checks.has("error_code")) {
            int errorCode= checks.getInt("error_code");
            String errorCodeScript = String.format("\t'error_code was %d': x => x.error_code == %d,%s",
                    errorCode, errorCode, newLine);
            checkBuilder.append(errorCodeScript);
        }

        return String.format("check(response%d, {%s%s});%s",
                requestIndex, newLine, checkBuilder, newLine);
    }
}