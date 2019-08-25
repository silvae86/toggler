package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import net.minidev.json.JSONArray;
import org.junit.After;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class ServicesControllerTest extends WithApplication {

    private Application fakeApp;

    @Override
    protected Application provideApplication() {
        MongoConfig.dropDatabase();
        fakeApp = new GuiceApplicationBuilder().build();
        return fakeApp;
    }

    @Test
    public void testProblemExampleConfiguration() throws IOException {

        // User Not Authenticated, should return unauthorized
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC");

        Result result = route(app, request);
        assertEquals(UNAUTHORIZED, result.status());

        // Authenticate as standard user
        request = new Http.RequestBuilder()
                .method(POST)
                .uri("/users/login");

        Map<String, String> authPayload = new HashMap<>();
        authPayload.put("username", ConfigFactory.load().getString("user.username"));
        authPayload.put("password", ConfigFactory.load().getString("user.password"));
        request.bodyForm(authPayload);

        result = route(app, request);

        assertEquals(OK, result.status());
        String APIKey = contentAsString(result);
        assertNotNull(APIKey);

        // Toggle does not exist on first request, return not found
        request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC")
                .header("X-API-Key", APIKey);

        request.headers(request.getHeaders().addHeader("X-API-Key", APIKey));

        result = route(app, request);
        assertEquals(NOT_FOUND, result.status());

        // Send the configuration to create several toggles
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Path baseConfigPath = Paths.get("test/mocks/problem_statement.yml");
        String baseConfig = baseConfigPath.toAbsolutePath().toString();
        File configFile = new File(baseConfig);

        request = new Http.RequestBuilder()
                .method(POST)
                .uri("/config")
                .bodyJson(mapper.readTree(configFile))
                .header("X-API-Key", APIKey);

        result = route(app, request);
        assertEquals(OK, result.status());


        // Toggle now exists, let us see if the toggle exists
        request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC")
                .header("X-API-Key", APIKey);

        result = route(app, request);
        assertEquals(OK, result.status());

        // isButtonBlue

        JSONArray isButtonBlueToggle = JsonPath.read(contentAsString(result), "$.toggles[?(@.name=='isButtonBlue')]");
        assertNotNull(isButtonBlueToggle);
        assertEquals(1, isButtonBlueToggle.size());

        HashMap<String, Object> object = (HashMap) isButtonBlueToggle.get(0);
        assertEquals(object.get("value"), false);

        // isButtonGreen
        JSONArray isButtonGreenToggle = JsonPath.read(contentAsString(result), "$.toggles[?(@.name=='isButtonGreen')]");
        assertNotNull(isButtonGreenToggle);
        assertEquals(1, isButtonGreenToggle.size());

        object = (HashMap) isButtonGreenToggle.get(0);
        assertEquals(object.get("value"), false);

        // isButtonRed
        JSONArray isButtonRedToggle = JsonPath.read(contentAsString(result), "$.toggles[?(@.name=='isButtonRed')]");
        assertNotNull(isButtonRedToggle);
        assertEquals(0, isButtonRedToggle.size());

        // Try to access again without the API Key
        request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC");

        result = route(app, request);
        assertEquals(UNAUTHORIZED, result.status());
    }

    @After
    public void teardown() {
        Helpers.stop(fakeApp);
    }
}
