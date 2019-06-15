package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import database.MongoConfig;
import org.junit.After;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.test.Helpers.*;

public class TogglesControllerTest extends WithApplication {

    private int howManyTogglesToCreate = 3000;
    private int howManyTogglesToDelete = 500;
    private Application fakeApp;

    @Override
    protected Application provideApplication() {
        fakeApp = new GuiceApplicationBuilder().build();
        MongoConfig.dropDatabase();

        Http.RequestBuilder request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), 0);

        for (int i = 0; i < howManyTogglesToCreate; i++) {
            String newToggleName = UUID.randomUUID().toString().replace("-", "");

            request = new Http.RequestBuilder()
                    .method(PUT)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ newToggleName);

            HashMap<String, String> payload = new HashMap<>();

            payload.put("name", newToggleName);
            payload.put("value", "false");
            request.bodyForm(payload);

            result = route(fakeApp,request);
            assertEquals(OK, result.status());
        }

        return fakeApp;
    }

    @Test
    public void createNewToggles() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .header("Accept", "application/json")
                .uri("/toggles");

        Result result = route(fakeApp, request);
        JsonNode o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), howManyTogglesToCreate);
    }

    @Test
    public void changeValueOnToggles() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), howManyTogglesToCreate);

        for(Iterator<JsonNode> it = o.iterator(); it.hasNext();)
        {
            JsonNode aToggle = it.next();

            assertTrue("Node should have a name text property", aToggle.get("name").isTextual());

            request = new Http.RequestBuilder()
                    .method(POST)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());

            HashMap<String, String> payload = new HashMap<>();

            payload.put("value", "true");
            request.bodyForm(payload);

            result = route(fakeApp,request);
            assertEquals(OK, result.status());

            request = new Http.RequestBuilder()
                    .method(GET)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());


            result = route(fakeApp,request);
            o = Json.parse(contentAsString(result));

            assertEquals("true", o.get("value").asText());
        }
    }

    @Test
    public void deleteToggles() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), howManyTogglesToCreate);

        int deletedToggles = 0;
        for(Iterator<JsonNode> it = o.iterator(); it.hasNext() && deletedToggles < howManyTogglesToDelete; deletedToggles++)
        {
            JsonNode aToggle = it.next();

            assertTrue("Node should have a name text property", aToggle.get("name").isTextual());

            request = new Http.RequestBuilder()
                    .method(DELETE)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());

            result = route(fakeApp,request);
            assertEquals(OK, result.status());

            request = new Http.RequestBuilder()
                    .method(GET)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());


            result = route(fakeApp,request);
            assertEquals(NOT_FOUND, result.status());
        }

        request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        result = route(fakeApp, request);
        assertEquals(OK, result.status());

        o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), howManyTogglesToCreate-howManyTogglesToDelete);
    }

    @Test
    public void deleteAllToggles() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), howManyTogglesToCreate);

        for(Iterator<JsonNode> it = o.iterator(); it.hasNext();)
        {
            JsonNode aToggle = it.next();

            assertTrue("Node should have a name text property", aToggle.get("name").isTextual());

            request = new Http.RequestBuilder()
                    .method(DELETE)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());

            result = route(fakeApp,request);
            assertEquals(OK, result.status());

            request = new Http.RequestBuilder()
                    .method(GET)
                    .header("Accept", "application/json")
                    .uri("/toggles/"+ aToggle.get("name").asText());


            result = route(fakeApp,request);
            assertEquals(NOT_FOUND, result.status());
        }

        request = new Http.RequestBuilder()
                .header("Accept", "application/json")
                .method(GET)
                .uri("/toggles");

        result = route(fakeApp, request);
        assertEquals(OK, result.status());

        o = Json.parse(contentAsString(result));

        assert(o.isArray());
        assertEquals(o.size(), 0);
    }

    @After
    public void teardown() {
        Helpers.stop(fakeApp);
        MongoConfig.dropDatabase();
    }
}
