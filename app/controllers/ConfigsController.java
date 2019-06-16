package controllers;

import database.MongoConfig;
import models.Config;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static play.mvc.Results.*;

public class ConfigsController {
    public Result get () {
        try {
            Config latestConfig = Config.getLatestConfig();
            if(latestConfig == null)
            {
                return notFound(Json.toJson("No configuration is set"));
            }
            else
            {
                return ok(Json.toJson(latestConfig));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result post (Http.Request request) {

        try {
            Config newConfig = new Config(request.body().asText());
            MongoConfig.datastore().save(newConfig);
            return ok(Json.toJson(newConfig));
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
