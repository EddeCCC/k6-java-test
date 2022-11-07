package poc.loadtest.mapper;

import org.json.JSONObject;

interface k6Mapper {

    String newLine = System.lineSeparator();

    String map(JSONObject request, int requestIndex);
}