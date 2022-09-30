package poc.loadtest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import poc.exception.UnknownRequestTypeException;

import java.util.LinkedList;
import java.util.List;

public class JSONScriptMapper {

    private final List<String> script;

    public JSONScriptMapper(){
        script = new LinkedList<>();
        String START = """
                import http from 'k6/http';
                import {check, sleep} from 'k6';
                                
                let config = JSON.parse(open('../config/config.json'));
                let baseUrl = config.baseURL;
                export let options = config.options;
                                
                export default function() {
                                
                """;
        script.add(START);
    }

    public List<String> mapConfig(JSONObject request) {
        String type = request.getString("type");
        switch (type) {
            case "GET" -> mapGetRequest(request);
            case "POST" -> mapPostRequest(request);
            case "PUT" -> mapPutRequest(request);
            case "DELETE" -> mapDeleteRequest(request);
            default -> throw new UnknownRequestTypeException(type);
        }
        return this.script;
    }

    private void mapGetRequest(JSONObject request) {
        String path = request.getString("path");
        String pathScript = "let response = http.get(baseUrl + '" + path + "');\n";

        JSONObject checks = request.getJSONObject("checks");
        Integer status = Integer.parseInt( checks.getString("status") );
        String statusScript = "\t'GET status was " + status + "': x => x.status == " + status + ",\n"; //PASST DAS?

        String checksScript = "check(response, {\n" + statusScript + "});\n";
        String endFunction = "sleep(1);\n}";

        script.add(pathScript);
        script.add(checksScript);
        script.add(endFunction);
        System.out.println("SCRIPT: " + script);
    }

    private void mapPostRequest(JSONObject request) {

    }

    private void mapPutRequest(JSONObject request) {

    }

    private void mapDeleteRequest(JSONObject request) {

    }

    private String mapCheck() {
        return "check TODO";
    }
}