package poc.loadtest.mapper;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownRequestTypeException;

@Component
public class HttpMapper implements k6Mapper {

    @Override
    public String map(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String type = request.getString("type");
        String method = switch (type) {
            case "GET" -> "get";
            case "POST" -> "post";
            case "PUT" -> "put";
            case "DELETE" -> "del";
            default -> throw new UnknownRequestTypeException(type);
        };

        if(request.has("payload")) {
            if(request.has("params")) {
                return String.format("%slet response%d = http.%s(baseURL + '%s', JSON.stringify(payload%d), params%d);%s",
                        newLine, requestIndex, method, path, requestIndex, requestIndex, newLine);
            }

            return String.format("%slet response%d = http.%s(baseURL + '%s', JSON.stringify(payload%d));%s",
                    newLine, requestIndex, method, path, requestIndex, newLine);
        }

        return String.format("%slet response%d = http.%s(baseURL + '%s');%s",
                newLine, requestIndex, method, path, newLine);
    }
}