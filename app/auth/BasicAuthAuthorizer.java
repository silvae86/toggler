package auth;

import core.RequestProcessor;
import models.roles.Admin;
import models.roles.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Optional;

public class BasicAuthAuthorizer extends Security.Authenticator {

    @Override
    public Optional<String> getUsername(Http.Request req) {
        // Do Authentication here
        // Returning NULL means request in not authorized

        try {
            String username = RequestProcessor.extractSingleValue(req, "username");
            String password = RequestProcessor.extractSingleValue(req, "password");

            if (User.auth(username, password) != null) {
                return Optional.of(username);
            } else if (Admin.auth(username, password) != null) {
                return Optional.of(username);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Result onUnauthorized(Http.Request req) {
        // Response for unauthenticated request
        return unauthorized("Invalid username or password combination");
    }
}