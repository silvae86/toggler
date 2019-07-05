package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import database.MongoConfig;
import io.swagger.annotations.Api;
import models.PermissionNode;
import models.concepts.Service;
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
            List<PermissionNode> allTogglesWithName = PermissionNode.findByName(name);
            if (allTogglesWithName.size() == 0)
            {
                return notFound(Json.toJson("PermissionNode with " + name + " not found."));
            }
            else
            {
                for (PermissionNode permissionNode : allTogglesWithName) {
                    MongoConfig.datastore().delete(permissionNode);
                }

                return ok(Json.toJson("All toggleInstances with name " + name + " deleted."));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result get (String name, String serviceName, String serviceVersion) {
        try {
            PermissionNode permissionNodeToGet = PermissionNode.findByNameServiceNameAndVersion(name, serviceName, serviceVersion);
            if (permissionNodeToGet == null)
            {
                return notFound(Json.toJson("PermissionNode with " + name + " not found."));
            }
            else
            {
                return ok(Json.toJson(permissionNodeToGet));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result set(Http.Request request, String toggleName, String serviceName, String serviceVersion) {

        PermissionNode permissionNodeToChange;

        try {
            permissionNodeToChange = PermissionNode.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion);
        } catch (Exception e)
        {
            return notFound("PermissionNode with name " + toggleName + " does not exist.");
        }

        try {
            Service.findByNameAndVersion(toggleName, serviceVersion);
        } catch (Exception e) {
            return notFound("Service with name " + serviceName + " does not exist.");
        }


        Map<String, String> data;
        try {
            data = RequestProcessor.extractSingleValueParameters(request, "value");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }

        boolean newValue = Boolean.parseBoolean(data.get("value"));

        if (permissionNodeToChange != null) {
            permissionNodeToChange.setDefaultValue(newValue);
            MongoConfig.datastore().save(permissionNodeToChange);
        }

        try {
            return ok(Json.toJson(PermissionNode.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion)));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }

    public Result create(Http.Request request) {
        Map<String, String> newToggleData;
        try{
            newToggleData = RequestProcessor.extractSingleValueParameters(request, "new_toggle_data");
        }
        catch(Exception e)
        {
            return badRequest(e.getMessage());
        }

        JsonNode newToggleDataJson = Json.parse(newToggleData.get("new_toggle_data"));
        String toggleName = newToggleData.get("name");
        String services = newToggleData.get("services");
        String decision = newToggleData.get("decision");

        final PermissionNode permissionNodeWithSameName = MongoConfig.datastore().createQuery(PermissionNode.class)
                .field("name").equal(toggleName)
                .get();

        // toggle already exists
        if (permissionNodeWithSameName != null)
        {
            return status(409, Json.toJson("A toggle with id " + toggleName + " already exists."));
        }
        else
        {
            PermissionNode newPermissionNode = new PermissionNode();
            //newPermissionNode.setService();
            MongoConfig.datastore().save(newPermissionNode);
            return ok(Json.toJson("New toggle with " + newPermissionNode + " created."));
        }
    }

    public Result index () {
        final Query<PermissionNode> query = MongoConfig.datastore().createQuery(PermissionNode.class);
        final List<PermissionNode> permissionNodes = query.asList();
        return ok(Json.toJson(permissionNodes));
    }
}
