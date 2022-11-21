package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParamsMapper implements k6Mapper {

    @Autowired
    private HeadersMapper headersMapper;

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject params = request.getJSONObject("params");
        StringBuilder paramsBuilder = new StringBuilder();

        if(params.has("headers")) {
            String headerScript = headersMapper.map(params, requestIndex);
            paramsBuilder.append(headerScript);
        }
        if(params.has("tags")) {
            String tagScript = this.mapTags(params);
            paramsBuilder.append(tagScript);
        }
        if(params.has("timeout")) {
            String timeoutScript = this.mapTimeout(params);
            paramsBuilder.append(timeoutScript);
        }
        if(params.has("cookies")) {
            String cookiesScript = this.mapCookies(params);
            paramsBuilder.append(cookiesScript);
        }

        return String.format("%svar params%d = {%s%s};%s",
                newLine, requestIndex, newLine, paramsBuilder, newLine);
    }

    private String mapTags(JSONObject params) {
        JSONObject tags = params.getJSONObject("tags");
        String tagsString = tags.toString();

        return String.format("tags: %s,%s",
                tagsString, newLine);
    }

    private String mapTimeout(JSONObject params) {
        String timeout = params.getString("timeout");

        return String.format("timeout: '%s',%s",
                timeout, newLine);
    }

    private String mapCookies(JSONObject params) {
        JSONObject cookies = params.getJSONObject("cookies");
        String cookiesString = cookies.toString();

        return String.format("cookies: %s,%s",
                cookiesString, newLine);
    }
}