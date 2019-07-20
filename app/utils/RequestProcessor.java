package utils;

import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;

public class RequestProcessor {

    public static Map<String, String> extractSingleValueParameters(Http.Request request, String... parameters) throws Exception {
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Map<String, String> extractedParameters = new HashMap<>();

        for (String param : parameters
        ) {
            if (data.get(param) != null) {
                if (data.get(param).length == 1) {
                    extractedParameters.put(param, data.get(param)[0]);
                } else {
                    throw new Exception(param + " can only have a single value.");
                }
            } else {
                throw new Exception(param + " is required but is not present in the request body.");
            }
        }

        return extractedParameters;
    }
}
