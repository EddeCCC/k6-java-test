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
            String contentType = headers.getString("content_type");
            String contentTypeScript = String.format("\t'Content-Type': '%s',%s",
                    contentType, newLine);
            headBuilder.append(contentTypeScript);
        }
        if(headers.has("timeout")) {
            String timeout = headers.getString("timeout");
            String timeoutScript = String.format("\t'Timeout': '%s',%s",
                    timeout, newLine);
            headBuilder.append(timeoutScript);
        }

        return String.format("headers: {%s%s},%s",
                newLine, headBuilder, newLine);
    }
}
