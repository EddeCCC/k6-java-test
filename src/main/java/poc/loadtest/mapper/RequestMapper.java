package poc.loadtest.mapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Component
public class RequestMapper implements k6Mapper {

    @Autowired
    private ParamsMapper paramsMapper;
    @Autowired
    private PayloadMapper payloadMapper;
    @Autowired
    private HttpMapper httpMapper;
    @Autowired
    private ChecksMapper checksMapper;

    public List<String> createScript(JSONObject config) {
        JSONArray requests = config.getJSONArray("requests");
        List<String> createdScript = new LinkedList<>();
        createdScript.add( startScript(config) );

        for(int i = 0; i < requests.length(); i++) {
            JSONObject currentRequest = requests.getJSONObject(i);

            if(!isRequestValid(currentRequest)) {
                System.out.println("Invalid request: " + i);
                continue;
            }
            String requestScript = this.map(currentRequest, i);
            createdScript.add(requestScript);
        }
        createdScript.add("}");
        return createdScript;
    }

    @Override
    public String map(JSONObject request, int requestIndex) {
        StringBuilder requestBuilder = new StringBuilder();

        if(request.has("params")) {
            String paramsScript = paramsMapper.map(request, requestIndex);
            requestBuilder.append(paramsScript);
        }
        if(request.has("payload")) {
            String payloadScript = payloadMapper.map(request, requestIndex);
            requestBuilder.append(payloadScript);
        }

        String httpScript = httpMapper.map(request, requestIndex);
        requestBuilder.append(httpScript);

        if(request.has("checks")) {
            String checksScript = checksMapper.map(request, requestIndex);
            requestBuilder.append(checksScript);
        }
        requestBuilder.append(sleepScript());

        return requestBuilder.toString();
    }

    private String startScript(JSONObject config) {
        String baseURL = config.getString("baseURL");
        String options = config.getJSONObject("options").toString();
        return """
                import http from 'k6/http';
                import {check, sleep} from 'k6';

                let baseURL = '%s';

                export let options = %s;
                                
                export default function() {
                """.formatted(baseURL, options);
    }

    private String sleepScript() {
        Random random = new Random();
        int duration = random.nextInt(5) + 1;
        return String.format("sleep(%d);%s",
                duration, newLine);
    }

    private Boolean isRequestValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }
}