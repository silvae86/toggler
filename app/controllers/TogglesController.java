package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import database.MongoConfig;
import io.swagger.annotations.Api;
import models.Service;
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
            List<Toggle> allTogglesWithName = Toggle.findByName(name);
            if (allTogglesWithName.size() == 0)
            {
                return notFound(Json.toJson("Toggle with " + name + " not found."));
            }
            else
            {
                for (Toggle toggle : allTogglesWithName) {
                    MongoConfig.datastore().delete(toggle);
                }

                return ok(Json.toJson("All toggles with name " + name + " deleted."));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result get (String name, String serviceName, String serviceVersion) {
        try {
            Toggle toggleToGet = Toggle.findByNameServiceNameAndVersion(name, serviceName, serviceVersion);
            if (toggleToGet == null)
            {
                return notFound(Json.toJson("Toggle with " + name + " not found."));
            }
            else
            {
                return ok(Json.toJson(toggleToGet));
            }
        }
        catch(Exception e)
        {
            return internalServerError(Json.toJson(e.getMessage()));
        }
    }

    public Result set(Http.Request request, String toggleName, String serviceName, String serviceVersion) {

        Toggle toggleToChange;

        try {
            toggleToChange = Toggle.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion);
        } catch (Exception e)
        {
            return notFound("Toggle with name " + toggleName + " does not exist.");
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

        if (toggleToChange != null) {
            toggleToChange.setValue(newValue);
            MongoConfig.datastore().save(toggleToChange);
        }

        try {
            return ok(Json.toJson(Toggle.findByNameServiceNameAndVersion(toggleName, serviceName, serviceVersion)));
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

        final Toggle toggleWithSameName = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(toggleName)
                .get();

        // toggle already exists
        if (toggleWithSameName != null)
        {
            return status(409, Json.toJson("A toggle with id " + toggleName + " already exists."));
        }
        else
        {
            Toggle newToggle = new Toggle(toggleName);
            MongoConfig.datastore().save(newToggle);
            return ok(Json.toJson("New toggle with " + newToggle + " created."));
        }
    }

    public Result index () {
        final Query<Toggle> query = MongoConfig.datastore().createQuery(Toggle.class);
        final List<Toggle> toggles = query.asList();
        return ok(Json.toJson(toggles));
    }
}
