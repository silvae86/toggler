package controllers;

import database.MongoConfig;
import models.ConfigChange;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Results.*;

public class ConfigsController {
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
            ConfigChange newConfigChange = new ConfigChange(request.body().asText());
            MongoConfig.datastore().save(newConfigChange);
            return ok(Json.toJson(newConfigChange));
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
