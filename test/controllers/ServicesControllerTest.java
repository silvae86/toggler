package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
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

        // Toggle does not exist on first request, return not found
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC");

        Result result = route(app, request);
        assertEquals(NOT_FOUND, result.status());


        // Send the configuration to create several toggles
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Path baseConfigPath = Paths.get("test/mocks/problem_statement.yml");
        String baseConfig = baseConfigPath.toAbsolutePath().toString();
        File configFile = new File(baseConfig);

        request = new Http.RequestBuilder()
                .method(POST)
                .uri("/config")
                .bodyJson(mapper.readTree(configFile));

        result = route(app, request);
        assertEquals(OK, result.status());


        // Toggle now exists, let us see if the toggle exists
        request = new Http.RequestBuilder()
                .method(GET)
                .uri("/service/ABC");

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
    }

    @After
    public void teardown() {
        Helpers.stop(fakeApp);
    }
}
