package controllers;

import com.google.inject.Inject;
import database.MongoConfig;
import io.swagger.annotations.*;
import models.Toggle;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the Toggles Service
 */

@Api
public class TogglesController extends Controller {
    @Inject play.data.FormFactory formFactory;

    public Result delete (Http.Request request, String name) {
        try {
            Toggle toggle = Toggle.findByName(name);
            if(toggle == null)
            {
                return notFound();
            }
            else
            {
                MongoConfig.datastore().delete(toggle);
                return ok(Json.toJson(toggle));
            }
        }
        catch(Exception e)
        {
            return internalServerError();
        }
    }

    public Result get (String name) {
        try {
            Toggle toggle = Toggle.findByName(name);
            if(toggle == null)
            {
                return notFound();
            }
            else
            {
                return ok(Json.toJson(toggle));
            }
        }
        catch(Exception e)
        {
            return internalServerError();
        }
    }

    public Result set (Http.Request request, String name) {
        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .limit(1).asList();

        if(toggles.size() == 1)
        {
            play.data.DynamicForm data = formFactory.form().bindFromRequest(request, "value");
            boolean value = Boolean.parseBoolean(data.get("value"));

            Toggle toggle = toggles.get(0);
            toggle.setValue(value);

            return ok(Json.toJson(toggles));
        }
        else
        {
            return notFound();
        }
    }

    public Result create (Http.Request request, String name) {
        play.data.DynamicForm data = formFactory.form().bindFromRequest(request, "value");
        boolean value = Boolean.parseBoolean(data.get("value"));

        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .limit(1)
                .asList();

        // toggle already exists
        if (toggles.size() == 1)
        {
            return status(409, Json.toJson("A toggle with id " + name + " already exists."));
        }
        else
        {
            Toggle newToggle = new Toggle(name, value);
            return ok(Json.toJson("A toggle with id " + name + " already exists."));
        }
    }

    public Result index () {
        final Query<Toggle> query = MongoConfig.datastore().createQuery(Toggle.class);
        final List<Toggle> toggles = query.asList();
        return ok(Json.toJson(toggles));
    }
}
