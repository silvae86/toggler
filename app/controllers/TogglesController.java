package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import core.RequestProcessor;
import database.MongoConfig;
import dev.morphia.query.Query;
import io.swagger.annotations.Api;
import models.database.Service;
import models.database.Toggle;
import models.exchange.ConfigNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This controller contains an action to handle HTTP requests
 * to the Toggles Service
 */

@Api
public class TogglesController extends Controller {
    public Result delete(String name) {
        try {
            Iterator<Toggle> allTogglesWithName = Toggle.findByName(name);
            if (!allTogglesWithName.hasNext()) {
                return notFound(Json.toJson("ConfigNode with " + name + " not found."));
            } else {
                while (allTogglesWithName.hasNext())
                    MongoConfig.datastore().delete(allTogglesWithName.next());
            }

            return ok(Json.toJson("All toggleInstances with toggleName " + name + " deleted."));
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result get(String name, String serviceName, String serviceVersion) {
        try {
            Toggle toggleInstance = Toggle.findByNameServiceNameAndVersion(name, serviceName, serviceVersion);
            if (toggleInstance == null) {
                return notFound(Json.toJson("ConfigNode with " + name + " not found."));
            } else {
                return ok(Json.toJson(toggleInstance));
            }
        } catch (Exception e) {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result set(Http.Request request, String toggleName, String serviceName, String serviceVersion) {

        Toggle toggleInstance;
        try {
            toggleInstance = Toggle.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion);
        } catch (Exception e) {
            return notFound("ConfigNode with toggleName " + toggleName + " does not exist.");
        }

        try {
            Service.findByNameAndVersion(toggleName, serviceVersion);
        } catch (Exception e) {
            return notFound("Service with toggleName " + serviceName + " does not exist.");
        }


        Map<String, String> data;
        try {
            data = RequestProcessor.extractSingleValueParameters(request, "defaultValue");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }

        boolean newValue = Boolean.parseBoolean(data.get("defaultValue"));

        if (toggleInstance != null) {
            toggleInstance.setValue(newValue);
            MongoConfig.datastore().save(toggleInstance);
        }

        try {
            return ok(Json.toJson(Toggle.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion)));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }

    public Result create(Http.Request request) {
        Map<String, String> newToggleData;
        try {
            newToggleData = RequestProcessor.extractSingleValueParameters(request, "new_toggle_data");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }

        JsonNode newToggleDataJson = Json.parse(newToggleData.get("new_toggle_data"));
        String toggleName = newToggleData.get("toggleName");
        String services = newToggleData.get("services");
        String decision = newToggleData.get("decision");

        final ConfigNode permissionNodeWithSameName = MongoConfig.datastore().createQuery(ConfigNode.class)
                .field("toggleName").equal(toggleName)
                .get();

        // toggle already exists
        if (permissionNodeWithSameName != null) {
            return status(409, Json.toJson("A toggle with id " + toggleName + " already exists."));
        } else {
            ConfigNode newPermissionNode = new ConfigNode();
            //newPermissionNode.setService();
            MongoConfig.datastore().save(newPermissionNode);
            return ok(Json.toJson("New toggle with " + newPermissionNode + " created."));
        }
    }

    public Result index() {
        final Query<ConfigNode> query = MongoConfig.datastore().createQuery(ConfigNode.class);
        final List<ConfigNode> permissionNodes = query.asList();
        return ok(Json.toJson(permissionNodes));
    }
}
