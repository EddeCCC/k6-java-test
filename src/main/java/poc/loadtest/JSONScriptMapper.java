package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownRequestTypeException;

import java.util.LinkedList;
import java.util.List;

@Component
public class JSONScriptMapper {

    private final String newLine = System.lineSeparator();

    public List<String> createScript(JSONArray requests, String localConfigURL) {
        List<String> script = new LinkedList<>();
        script.add( startScript(localConfigURL) );

        for(int i = 0; i < requests.length(); i++) {
            JSONObject currentRequest = requests.getJSONObject(i);

            if(!isRequestValid(currentRequest)) {
                System.out.println("Invalid request: " + i);
                continue;
            }
            String requestScript = mapRequest(currentRequest, i);
            script.add(requestScript);
        }
        script.add("}");
        return script;
    }

    private String mapRequest(JSONObject request, int requestIndex) {
        StringBuilder requestBuilder = new StringBuilder();

        if(request.has("params")) {
            JSONObject params = request.getJSONObject("params");
            String paramsScript = mapParams(params, requestIndex);
            requestBuilder.append(paramsScript);
        }
        if(request.has("payload")) {
            JSONObject payload = request.getJSONObject("payload");
            String payloadScript = mapPayload(payload, requestIndex);
            requestBuilder.append(payloadScript);
        }

        String httpScript = mapHttpRequest(request, requestIndex);
        requestBuilder.append(httpScript);

        if(request.has("checks")) {
            JSONObject checks = request.getJSONObject("checks");
            String type = request.getString("type");
            String checksScript = mapCheck(checks, requestIndex, type);
            requestBuilder.append(checksScript);
        }
        requestBuilder.append(sleep());

        return requestBuilder.toString();
    }

    private String mapHttpRequest(JSONObject request, int requestIndex) {
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

    private String mapParams(JSONObject params, int requestIndex) {
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

    private String mapPayload(JSONObject payload, int requestIndex) {
        String payloadString = payload.toString();
        return String.format("%svar payload%d = %s%s",
                newLine, requestIndex, payloadString, newLine);
    }

    private String mapCheck(JSONObject checks, int requestIndex, String type) {
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

    private String startScript(String localConfigURL) {
        return """
                import http from 'k6/http';
                import {check, sleep} from 'k6';
                                
                let config = JSON.parse(open('%s'));
                let baseURL = config.baseURL;
                
                export let options = config.options;
                                
                export default function() {
                """.formatted(localConfigURL);
    }

    private Boolean isRequestValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }

    private String sleep() {
        int duration = (int)(Math.random()*2) + 1; //1-3
        return String.format("sleep(%d);%s",
                duration, newLine);
    }
}