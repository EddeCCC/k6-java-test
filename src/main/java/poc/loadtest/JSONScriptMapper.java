package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import poc.exception.UnknownRequestTypeException;

import javax.print.attribute.standard.JobName;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class JSONScriptMapper {

    private final List<String> script;
    private final String newLine = System.lineSeparator();
    private final String res = "response";

    public JSONScriptMapper(String configURL) {
        script = new LinkedList<>();
        String start = """
                import http from 'k6/http';
                import {check, sleep} from 'k6';
                                
                let config = JSON.parse(open('%s'));
                let baseURL = config.baseURL;
                
                export let options = config.options;
                                
                export default function() {
                """.formatted(configURL);
        script.add(start);
    }

    public List<String> createScript(JSONArray requests) {
        for(int i = 0; i < requests.length(); i++) {
            JSONObject currentRequest = requests.getJSONObject(i);

            if(!isRequestValid(currentRequest)) {
                System.out.println("Invalid request: " + i);
                continue;
            }
            String type = currentRequest.getString("type");

            switch (type) {
                case "GET" -> mapGetRequest(currentRequest, i);
                case "POST" -> mapPostRequest(currentRequest, i);
                case "PUT" -> mapPutRequest(currentRequest, i);
                case "DELETE" -> mapDeleteRequest(currentRequest, i);
                default -> throw new UnknownRequestTypeException(type);
            }
        }
        script.add("}");
        return this.script;
    }

    private void mapGetRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String responseVariable = res + requestIndex;
        String paramsScript = "";
        String checkScript = "";

        if(request.has("params")) {
            JSONObject params = request.getJSONObject("params");
            paramsScript = mapParams(params);
        }
        String httpScript = String.format("%slet %s = http.get(baseURL + '%s', {%s%s});%s",
                newLine, responseVariable, path, newLine, paramsScript, newLine);

        if(request.has("checks")) checkScript = mapCheck(request, responseVariable);

        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(1));
    }

    private void mapPostRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String responseVariable = res + requestIndex;
        String paramsScript = "";

        if(request.has("params")) {
            JSONObject params = request.getJSONObject("params");
            paramsScript = mapParams(params);
        }

        String payload = request.getJSONObject("payload").toString();
        String payloadScript = String.format("%svar payload%d = %s%s",
                newLine,requestIndex, payload, newLine);

        String httpScript = String.format("%slet %s = http.post(baseURL + '%s', JSON.stringify(payload%d), {%s%s});%s",
                newLine, responseVariable, path, requestIndex, newLine, paramsScript, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, responseVariable);

        script.add(payloadScript);
        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(2));
    }

    private void mapPutRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String responseVariable = res + requestIndex;
        String paramsScript = "";

        if(request.has("params")) {
            JSONObject params = request.getJSONObject("params");
            paramsScript = mapParams(params);
        }

        String payload = request.getJSONObject("payload").toString();
        String payloadScript = String.format("%svar payload%d = %s%s",
                newLine,requestIndex, payload, newLine);

        String httpScript = String.format("%slet %s = http.put(baseURL + '%s', JSON.stringify(payload%d), {%s%s});%s",
                newLine, responseVariable, path, requestIndex, newLine, paramsScript, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, responseVariable);

        script.add(payloadScript);
        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(2));
    }

    private void mapDeleteRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String responseVariable = res + requestIndex;

        String httpScript = String.format("%slet %s = http.del(baseURL + '%s');%s",
                newLine, responseVariable, path, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, responseVariable);

        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(1));
    }

    private String mapParams(JSONObject params) {
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
        return paramsBuilder.toString();
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

    private String mapCheck(JSONObject request, String response) {
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

        return String.format("check(%s, {%s%s});%s",
                response, newLine, checkBuilder, newLine);
    }

    private Boolean isRequestValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }

    private String sleep(int amount) {
        return String.format("sleep(%d);%s",
                amount, newLine);
    }
}