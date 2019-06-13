package controllers;

import com.google.inject.Inject;
import database.MongoConfig;
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


public class TogglesController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    @Inject play.data.FormFactory formFactory;

    public Result delete (String name) {
        return ok(views.html.index.render());
    }

    public Result get (String name) {
        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .asList();

        return ok(Json.toJson(toggles));
    }

    public Result edit (String name) {
        return ok(views.html.index.render());
    }

    public Result create (Http.Request request) {
        play.data.DynamicForm data = formFactory.form().bindFromRequest(request, "name", "value");

        String name = data.get("name");
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
