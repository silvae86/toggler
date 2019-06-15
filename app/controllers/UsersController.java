package controllers;

import com.google.inject.Inject;
import database.MongoConfig;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Map;

public class UsersController extends Controller {
    public Result login(Http.Request request) {
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String username = data.get("username").toString();
        String password = data.get("password").toString();

        User authenticatingUser = MongoConfig.datastore().find(User.class)
                .field("username").equal(username)
                .limit(1).get();

        if(authenticatingUser == null)
        {
            return notFound();
        }
        else
        {
            if(authenticatingUser.auth(password))
            {
                return ok();
            }
            else
            {
                return unauthorized();
            }
        }
    }
}
