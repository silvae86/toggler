package controllers;

import database.MongoConfig;
import models.database.Service;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class ServicesController extends Controller {

    public Result create (String name, String version)
    {
        try {
            Service newService = new Service(name, version);

            try{
                MongoConfig.datastore().save(newService);
            }
            catch(Exception e)
            {
                return notAcceptable(Json.toJson("Service with " + name + " and version " + version + " already exists."));
            }

            Service createdService = Service.findByNameAndVersion(name, version);

            return ok(Json.toJson(createdService));
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result get(String name, String version) {
        try {
            Service service = Service.findByNameAndVersion(name, version);
            if(service == null)
            {
                return notFound(Json.toJson("Service with " + name + " and version " + version + " not found."));
            }
            else
            {
                return ok(Json.toJson(service));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
