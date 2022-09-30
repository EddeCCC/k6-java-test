package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import poc.exception.UnknownRequestTypeException;

import java.util.LinkedList;
import java.util.List;

public class JSONScriptMapper {

    private final List<String> script;
    private final String newLine = System.lineSeparator();
    private final String res = "response";

    public JSONScriptMapper(String optionsURL) {
        script = new LinkedList<>();
        String start = """
                import http from 'k6/http';
                import {check, sleep} from 'k6';
                                
                let config = JSON.parse(open('..%s'));
                let baseUrl = config.baseURL;
                
                export let options = config.options;
                                
                export default function() {
                """.formatted(optionsURL);
        script.add(start);
    }

    public List<String> createScript(JSONArray requests) {
        for(int i = 0; i < requests.length(); i++) {
            JSONObject currentRequest = requests.getJSONObject(i);

            if(!isConfigValid(currentRequest)) {
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
        String response = res + requestIndex;

        String httpScript = String.format("%slet %s = http.get(baseUrl + '%s');%s",
                newLine, response, path, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, response);

        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(1));
    }

    private void mapPostRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String response = res + requestIndex;
        String headerScript = "";

        if(request.has("headers")) {
            JSONObject header = request.getJSONObject("headers");
            headerScript = mapHeader(header);
        }

        String payload = request.getJSONObject("payload").toString();
        String payloadScript = String.format("%svar payload%d = %s%s",
                newLine,requestIndex, payload, newLine);

        String httpScript = String.format("%slet %s = http.post(baseURL + '%s', JSON.stringify(payload%d), {%s%s});%s",
                newLine, response, path, requestIndex, newLine, headerScript, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, response);

        script.add(payloadScript);
        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(2));
    }

    private void mapPutRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String response = res + requestIndex;
        String headerScript = "";

        if(request.has("headers")) {
            JSONObject header = request.getJSONObject("headers");
            headerScript = mapHeader(header);
        }

        String payload = request.getJSONObject("payload").toString();
        String payloadScript = String.format("%svar payload%d = %s%s",
                newLine,requestIndex, payload, newLine);

        String httpScript = String.format("%slet %s = http.put(baseURL + '%s', JSON.stringify(payload%d), {%s%s});%s",
                newLine, response, path, requestIndex, newLine, headerScript, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, response);

        script.add(payloadScript);
        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(2));
    }

    private void mapDeleteRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String response = res + requestIndex;

        String httpScript = String.format("%slet %s = http.del(baseUrl + '%s');%s",
                newLine, response, path, newLine);
        String checkScript = "";

        if(request.has("checks")) checkScript = mapCheck(request, response);

        script.add(httpScript);
        script.add(checkScript);
        script.add(sleep(1));
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

        return String.format("headers: {%s%s}%s",
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
                Integer minLength = Integer.parseInt( body.getString("min-length") );
                String minLengthScript = String.format("\t'%s body size > %d': x => x.body && x.body.length > %d,%s",
                        type, minLength, minLength, newLine);
                checkBuilder.append(minLengthScript);
            }
            //More checks with body...
        }

        return String.format("check(%s, {%s%s});%s",
                response, newLine, checkBuilder, newLine);
    }

    private boolean isConfigValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }

    private String sleep(int amount) {
        return String.format("sleep(%d);%s",
                amount, newLine);
    }
}