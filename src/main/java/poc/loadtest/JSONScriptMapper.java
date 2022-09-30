package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import poc.exception.UnknownRequestTypeException;

import java.util.LinkedList;
import java.util.List;

public class JSONScriptMapper {

    private final List<String> script;

    private final String newLine = System.lineSeparator();

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

    public List<String> mapConfig(JSONArray requests) {
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
        script.add(endScript());
        return this.script;
    }

    private void mapGetRequest(JSONObject request, int requestIndex) {
        String path = request.getString("path");
        String response = "response" + requestIndex;

        String pathScript = String.format("%slet %s = http.get(baseUrl + '%s');%s",
                newLine, response, path, newLine);
        String checksScript = "";

        if(request.has("checks")) checksScript = mapCheck(request, response);

        script.add(pathScript);
        script.add(checksScript);
    }

    private void mapPostRequest(JSONObject request, int requestIndex) {

    }

    private void mapPutRequest(JSONObject request, int requestIndex) {

    }

    private void mapDeleteRequest(JSONObject request, int requestIndex) {

    }

    private String endScript() {
        return String.format("sleep(1);%s}", newLine);
    }

    private String mapCheck(JSONObject request, String response) {
        JSONObject checks = request.getJSONObject("checks");
        String type = request.getString("type");
        StringBuilder checkBuilder = new StringBuilder();

        if(checks.has("status")) {
            Integer status = Integer.parseInt( checks.getString("status") );
            String statusScript = String.format("\t'%s status was %s': x => x.status && x.status == %s,%s",
                    type, status, status, newLine);
            checkBuilder.append(statusScript);
        }
        if(checks.has("body")) {
            JSONObject body = checks.getJSONObject("body");

            if(body.has("min-length")) {
                Integer minLength = Integer.parseInt( body.getString("min-length") );
                String minLengthScript = String.format("\t'%s body size > %d': x => x.body.length > %d,%s",
                        type, minLength, minLength, newLine);
                checkBuilder.append(minLengthScript);
            }
            //More checks with body...
        }

        return String.format("check(%s, {%s%s});%s", response, newLine, checkBuilder, newLine);
    }

    private boolean isConfigValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }
}