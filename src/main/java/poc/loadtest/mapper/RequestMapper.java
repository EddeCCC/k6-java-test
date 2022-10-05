package poc.loadtest.mapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

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

    public List<String> createScript(JSONArray requests, String localConfigURL) {
        List<String> script = new LinkedList<>();
        script.add( configScript(localConfigURL) );

        for(int i = 0; i < requests.length(); i++) {
            JSONObject currentRequest = requests.getJSONObject(i);

            if(!isRequestValid(currentRequest)) {
                System.out.println("Invalid request: " + i);
                continue;
            }
            String requestScript = this.map(currentRequest, i);
            script.add(requestScript);
        }
        script.add("}");
        return script;
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

    private String configScript(String localConfigURL) {
        return """
                import http from 'k6/http';
                import {check, sleep} from 'k6';
                                
                let config = JSON.parse(open('%s'));
                let baseURL = config.baseURL;
                
                export let options = config.options;
                                
                export default function() {
                """.formatted(localConfigURL);
    }

    private String sleepScript() {
        int duration = (int)(Math.random()*2) + 1; //1-3
        return String.format("sleep(%d);%s",
                duration, newLine);
    }

    private Boolean isRequestValid(JSONObject request) {
        return request.has("type") && request.has("path");
    }
}