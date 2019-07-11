package controllers;

import org.junit.After;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class HomeControllerTest extends WithApplication {

    private Application fakeApp;

    @Override
    protected Application provideApplication() {
        fakeApp = new GuiceApplicationBuilder().build();
        return fakeApp;
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @After
    public void teardown() {
        Helpers.stop(fakeApp);
    }
}
