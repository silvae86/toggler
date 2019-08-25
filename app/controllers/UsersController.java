package controllers;

import models.auth.APIToken;
import com.typesafe.config.ConfigFactory;
import core.RequestProcessor;
import database.MongoConfig;
import models.roles.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class UsersController extends Controller {

    public Result login(Http.Request req) {

        String username;
        String password;

        try {
            username = RequestProcessor.extractSingleValue(req, "username");
            password = RequestProcessor.extractSingleValue(req, "password");

            APIToken token = User.auth(username, password);

            if(token != null)
            {
                return ok(token.getValue());
            }
            else
            {
                return unauthorized();
            }
        } catch (Exception e) {
            return badRequest(e.getMessage());
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
