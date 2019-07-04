package controllers;

import database.MongoConfig;
import models.ConfigChange;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;

import static play.mvc.Results.*;

public class ConfigsController {

//    public Result create (Http.Request request, String name) {
//        Map<String, String> data;
//        try{
//            data = RequestProcessor.extractSingleValueParameters(request, "value");
//        }
//        catch(Exception e)
//        {
//            return badRequest(e.getMessage());
//        }
//
//        boolean value = Boolean.parseBoolean(data.get("value"));
//
//        final Toggle toggleWithSameName = MongoConfig.datastore().createQuery(Toggle.class)
//                .field("name").equal(name).get();
//
//        // toggle already exists
//        if (toggleWithSameName != null)
//        {
//            return status(409, Json.toJson("A toggle with id " + name + " already exists."));
//        }
//        else
//        {
//            Toggle newToggle = new Toggle(name, value);
//            MongoConfig.datastore().save(newToggle);
//            return ok(Json.toJson("New toggle with " + name + " created."));
//        }
//    }
//
//    public Result index () {
//        final Query<Toggle> query = MongoConfig.datastore().createQuery(Toggle.class);
//        final List<Toggle> toggles = query.asList();
//        return ok(Json.toJson(toggles));
//    }

    public Result get () {
        try {
            ConfigChange latestConfigChange = ConfigChange.getLatestConfig();
            if(latestConfigChange == null)
            {
                return notFound(Json.toJson("No configuration is set"));
            }
            else
            {
                return ok(Json.toJson(latestConfigChange));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result update (Http.Request request) {

        try {
            Optional<ConfigChange> newConfigChange = request.body().parseJson(ConfigChange.class);

            MongoConfig.datastore().save(newConfigChange);
            return ok(Json.toJson(newConfigChange));
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
