package controllers;

import com.google.inject.Inject;
import database.MongoConfig;
import io.swagger.annotations.Api;
import models.Toggle;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.RequestProcessor;

import java.util.List;
import java.util.Map;

/**
 * This controller contains an action to handle HTTP requests
 * to the Toggles Service
 */

@Api
public class TogglesController extends Controller {
    @Inject play.data.FormFactory formFactory;

    public Result delete (String name) {
        try {
            Toggle toggle = Toggle.findByName(name);
            if(toggle == null)
            {
                return notFound(Json.toJson("Toggle with " + name + " not found."));
            }
            else
            {
                MongoConfig.datastore().delete(toggle);
                return ok(Json.toJson(toggle));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result get (String name, String serviceName, String serviceVersion) {
        try {
            Toggle toggle = Toggle.findByName(name);
            if(toggle == null)
            {
                return notFound(Json.toJson("Toggle with " + name + " not found."));
            }
            else
            {
                return ok(Json.toJson(toggle));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result set (Http.Request request, String name) {
        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .limit(1).asList();

        if(toggles.size() == 1)
        {
            Map<String, String> data;
            try{
                data = RequestProcessor.extractSingleValueParameters(request, "value");
            }
            catch(Exception e)
            {
                return badRequest(e.getMessage());
            }

            boolean value = Boolean.parseBoolean(data.get("value"));

            Toggle toggle = toggles.get(0);
            toggle.setValue(value);
            MongoConfig.datastore().save(toggle);

            try{
                return ok(Json.toJson(Toggle.findByName(name)));
            }
            catch(Exception e)
            {
                return internalServerError(e.getMessage());
            }
        }
        else
        {
            return notFound(Json.toJson("Toggle with name " + name + " not found."));
        }
    }

    public Result create (Http.Request request, String name) {
        Map<String, String> data;
        try{
            data = RequestProcessor.extractSingleValueParameters(request, "value");
        }
        catch(Exception e)
        {
            return badRequest(e.getMessage());
        }

        boolean value = Boolean.parseBoolean(data.get("value"));

        final Toggle toggleWithSameName = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name).get();

        // toggle already exists
        if (toggleWithSameName != null)
        {
            return status(409, Json.toJson("A toggle with id " + name + " already exists."));
        }
        else
        {
            Toggle newToggle = new Toggle(name, value);
            MongoConfig.datastore().save(newToggle);
            return ok(Json.toJson("New toggle with " + name + " created."));
        }
    }

    public Result index () {
        final Query<Toggle> query = MongoConfig.datastore().createQuery(Toggle.class);
        final List<Toggle> toggles = query.asList();
        return ok(Json.toJson(toggles));
    }
}
