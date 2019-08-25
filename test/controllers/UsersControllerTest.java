package controllers;

import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import org.junit.After;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.HttpVerbs.POST;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class UsersControllerTest extends WithApplication {

    private Application fakeApp;

    @Override
    protected Application provideApplication() {
        MongoConfig.dropDatabase();
        fakeApp = new GuiceApplicationBuilder().build();
        return fakeApp;
    }

    @Test
    public void loginAdministratorAndCreateUsers() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/users/login");

        HashMap<String, String> loginFormData = new HashMap<>();

        // invalid login
        loginFormData.put("username", ConfigFactory.load().getString("admin.username"));
        loginFormData.put("password", "INVALID_PASSWORD");
        request.bodyForm(loginFormData);
        Result result = route(app, request);
        assertEquals(UNAUTHORIZED, result.status());

        //valid login
        loginFormData.put("username", ConfigFactory.load().getString("admin.username"));
        loginFormData.put("password", ConfigFactory.load().getString("admin.password"));
        request.bodyForm(loginFormData);
        result = route(app, request);

        //valid login
        loginFormData.put("username", ConfigFactory.load().getString("user.username"));
        loginFormData.put("password", ConfigFactory.load().getString("user.password"));
        request.bodyForm(loginFormData);
        result = route(app, request);

        assertEquals(OK, result.status());
    }

    @After
    public void teardown() {
        Helpers.stop(fakeApp);
    }
}
