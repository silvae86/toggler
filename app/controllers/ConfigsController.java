package controllers;

import database.MongoConfig;
import models.exchange.Config;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Results.*;

public class ConfigsController {

//    public Result create (Http.Request request, String toggleName) {
//        Map<String, String> data;
//        try{
//            data = RequestProcessor.extractSingleValueParameters(request, "defaultValue");
//        }
//        catch(Exception e)
//        {
//            return badRequest(e.getMessage());
//        }
//
//        boolean defaultValue = Boolean.parseBoolean(data.get("defaultValue"));
//
//        final ConfigNode toggleWithSameName = MongoConfig.datastore().createQuery(ConfigNode.class)
//                .field("toggleName").equal(toggleName).get();
//
//        // toggle already exists
//        if (toggleWithSameName != null)
//        {
//            return status(409, Json.toJson("A toggle with id " + toggleName + " already exists."));
//        }
//        else
//        {
//            ConfigNode newToggle = new ConfigNode(toggleName, defaultValue);
//            MongoConfig.datastore().save(newToggle);
//            return ok(Json.toJson("New toggle with " + toggleName + " created."));
//        }
//    }
//
//    public Result index () {
//        final Query<ConfigNode> query = MongoConfig.datastore().createQuery(ConfigNode.class);
//        final List<ConfigNode> toggleInstances = query.asList();
//        return ok(Json.toJson(toggleInstances));
//    }

    public Result get() {
        try {
            Config latestConfigChange = Config.getLatestConfig();
            if (latestConfigChange == null) {
                return notFound(Json.toJson("No configuration is set"));
            } else {
                return ok(Json.toJson(latestConfigChange));
            }
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result update(Http.Request request) {

        try {
            Config newConfigChange = request.body().parseJson(Config.class).get();

            MongoConfig.datastore().save(newConfigChange);

            return ok(Json.toJson(newConfigChange));
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
