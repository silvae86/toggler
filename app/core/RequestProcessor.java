package core;

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
                    throw new Exception(param + " can only have a single defaultValue.");
                }
            } else {
                throw new Exception(param + " is required but is not present in the request body.");
            }
        }

        return extractedParameters;
    }

    public static String extractSingleValue(Http.Request request, String... parameters) throws Exception {
        if (parameters.length == 1) {
            String parameterToGet = parameters[0];
            return extractSingleValueParameters(request, parameterToGet).get(parameterToGet);
        } else {
            throw new Exception("More than one parameter requested!");
        }
    }
}
