package controllers;

import auth.APIToken;
import auth.BasicAuthAuthorizer;
import com.typesafe.config.ConfigFactory;
import core.RequestProcessor;
import database.MongoConfig;
import models.roles.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class UsersController extends Controller {

    @Security.Authenticated(BasicAuthAuthorizer.class)
    public Result auth(Http.Request req) {

        String username;
        String password;

        try {
            username = RequestProcessor.extractSingleValue(req, "username");
            password = RequestProcessor.extractSingleValue(req, "password");

            User.auth(username, password);

            return ok();
        } catch (Exception e) {
            return badRequest("Error interpreting username or password parameters");
        }
    }

    public Result registerUser(Http.Request req) {
        String username;
        String password;

        try {
            username = RequestProcessor.extractSingleValue(req, "username");
            password = RequestProcessor.extractSingleValue(req, "password");
        } catch (Exception e) {
            return badRequest("Error interpreting username or password parameters");
        }

        User user = User.findByUsername(username);

        if (user != null) {
            return status(409, "User with username " + username + " already exists.");
        } else {
            user = new User(username, password);
            MongoConfig.datastore().save(user);
            APIToken newToken = new APIToken(ConfigFactory.load().getInt("auth.token_validity_secs"), user);

            return ok(Json.toJson(newToken));
        }
    }
}
