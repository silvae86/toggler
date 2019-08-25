package controllers;

import auth.APITokenAuthorizer;
import models.database.Service;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class ServicesController extends Controller {
    @Security.Authenticated(APITokenAuthorizer.class)
    public Result getServiceByNameAndVersion(String name, String version) {
        try {
            Service service = Service.findByNameAndVersion(name, version);

            if (service == null) {
                return notFound(Json.toJson("Service with name " + name + " and version " + version + " not found."));
            } else {
                return ok(Json.toJson(service));
            }
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    @Security.Authenticated(APITokenAuthorizer.class)
    public Result getServiceByName(String name) {
        try {
            Service service = Service.findByName(name);

            if (service == null) {
                return notFound(Json.toJson("Service with name " + name + " not found."));
            } else {
                return ok(Json.toJson(service));
            }
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }
}
