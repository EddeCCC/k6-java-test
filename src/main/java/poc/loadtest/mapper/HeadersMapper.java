package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class HeadersMapper implements k6Mapper {

    @Override
    public String map(JSONObject params, int requestIndex) {
        JSONObject headers = params.getJSONObject("headers");
        StringBuilder headBuilder = new StringBuilder();

        if(headers.has("content_type")) {
            String contentTypeScript = this.mapContentType(headers);
            headBuilder.append(contentTypeScript);
        }

        return String.format("headers: {%s%s},%s",
                newLine, headBuilder, newLine);
    }

    private String mapContentType(JSONObject headers) {
        String contentType = headers.getString("content_type");
        return String.format("\t'Content-Type': '%s',%s",
                contentType, newLine);
    }
}