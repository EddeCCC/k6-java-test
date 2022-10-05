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
            JSONObject tags = params.getJSONObject("tags");
            String tagScript = mapTags(tags);
            paramsBuilder.append(tagScript);
        }

        return String.format("%svar params%d = {%s%s};%s",
                newLine, requestIndex, newLine, paramsBuilder, newLine);
    }

    private String mapTags(JSONObject tags) {
        String tagsString = tags.toString();

        return String.format("tags: %s,%s",
                tagsString, newLine);
    }
}