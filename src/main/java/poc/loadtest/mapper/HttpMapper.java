package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownRequestTypeException;

@Component
public class HttpMapper implements K6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String type = request.getString("type").toUpperCase();

        String method = switch (type) {
            case "GET" -> "get";
            case "POST" -> "post";
            case "PUT" -> "put";
            case "DELETE" -> "del";
            default -> throw new UnknownRequestTypeException(type);
        };

        String extraParams = "";
        if(request.has("payload") || request.has("params")) {
            if(request.has("payload") && request.has("params")) {
                extraParams = String.format(", JSON.stringify(payload%d), params%d",
                        requestIndex, requestIndex);
            }
            else if(request.has("payload"))
                extraParams = String.format(", JSON.stringify(payload%d)", requestIndex);

            else
                extraParams = String.format(", params%d", requestIndex);
        }

        return String.format("%slet response%d = http.%s(baseURL + '%s'%s);%s",
                newLine, requestIndex, method, path, extraParams, newLine);
    }
}