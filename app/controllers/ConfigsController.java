package controllers;

import database.MongoConfig;
import models.ConfigChange;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

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
//        final PermissionNode toggleWithSameName = MongoConfig.datastore().createQuery(PermissionNode.class)
//                .field("name").equal(name).get();
//
//        // toggle already exists
//        if (toggleWithSameName != null)
//        {
//            return status(409, Json.toJson("A toggle with id " + name + " already exists."));
//        }
//        else
//        {
//            PermissionNode newToggle = new PermissionNode(name, value);
//            MongoConfig.datastore().save(newToggle);
//            return ok(Json.toJson("New toggle with " + name + " created."));
//        }
//    }
//
//    public Result index () {
//        final Query<PermissionNode> query = MongoConfig.datastore().createQuery(PermissionNode.class);
//        final List<PermissionNode> toggleInstances = query.asList();
//        return ok(Json.toJson(toggleInstances));
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
            ConfigChange newConfigChange = request.body().parseJson(ConfigChange.class).get();

            MongoConfig.datastore().save(newConfigChange);
            newConfigChange.apply();

            return ok(Json.toJson(newConfigChange));
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
