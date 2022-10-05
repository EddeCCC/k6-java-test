package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ParamsMapper implements k6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        JSONObject params = request.getJSONObject("params");
        StringBuilder paramsBuilder = new StringBuilder();

        if(params.has("headers")) {
            JSONObject header = params.getJSONObject("headers");
            String headerScript = mapHeader(header);
            paramsBuilder.append(headerScript);
        }
        if(params.has("tags")) {
            JSONObject tags = params.getJSONObject("tags");
            String tagScript = mapTags(tags);
            paramsBuilder.append(tagScript);
        }

        return String.format("%svar params%d = {%s%s%s};",
                newLine, requestIndex, newLine, paramsBuilder, newLine);
    }

    private String mapTags(JSONObject tags) {
        String tagsString = tags.toString();

        return String.format("tags: %s%s,%s",
                newLine, tagsString, newLine);
    }

    private String mapHeader(JSONObject header) {
        StringBuilder headBuilder = new StringBuilder();

        if(header.has("content-type")) {
            String contentType = header.getString("content-type");
            String contentTypeScript = String.format("'Content-Type': '%s',%s",
                    contentType, newLine);
            headBuilder.append(contentTypeScript);
        }
        // More header options

        return String.format("headers: {%s%s},%s",
                newLine, headBuilder, newLine);
    }
}