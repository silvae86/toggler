package controllers;

import database.MongoConfig;
import models.roles.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.RequestProcessor;

import java.util.Map;

public class UsersController extends Controller {
    public Result login(Http.Request request) {
        Map<String, String> data;
        try {
            data = RequestProcessor.extractSingleValueParameters(request, "username", "password");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }

        String username = data.get("username");
        String password = data.get("password");

        User authenticatingUser = MongoConfig.datastore().find(User.class)
                .field("username").equal(username)
                .limit(1).get();

        if (authenticatingUser == null) {
            return notFound();
        } else {
            if (authenticatingUser.auth(password)) {
                return ok();
            } else {
                return unauthorized();
            }
        }
    }
}
