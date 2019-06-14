package controllers;

import com.google.inject.Inject;
import database.MongoConfig;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class UsersController extends Controller {
    @Inject
    play.data.FormFactory formFactory;

    public Result login() {
        Http.Request request = request();
        play.data.DynamicForm data = formFactory.form().bindFromRequest(request, "username", "password");
        String username = data.get("username");
        String password = data.get("password");

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
