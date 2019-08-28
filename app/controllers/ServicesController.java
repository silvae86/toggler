package controllers;

import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import auth.APITokenAuthorizer;
import models.database.Service;
import play.libs.Json;
import play.mvc.*;

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

    @Security.Authenticated(APITokenAuthorizer.class)
    public WebSocket subscribe(Http.Request originalHttpRequest, String name, String version) {
        return WebSocket.Text.accept(
                request -> {
                    // Just ignore the input
                    Sink<String, ?> in = Sink.ignore();

                    // Send a single 'Hello!' message and close
                    Source<String, ?> out = Source.single("Hello, name is " + name + " and version is " + version + " !");

                    return Flow.fromSinkAndSource(in, out);
                });
    }

    @Security.Authenticated(APITokenAuthorizer.class)
    public WebSocket subscribe(Http.Request originalHttpRequest, String name) {
        return WebSocket.Text.accept(
                request -> {
                    // Just ignore the input
                    Sink<String, ?> in = Sink.ignore();

                    // Send a single 'Hello!' message and close
                    Source<String, ?> out = Source.single("Hello, name is " + name + "!");

                    return Flow.fromSinkAndSource(in, out);
                });
    }
}
