package auth;

import core.RequestProcessor;
import models.auth.APIToken;
import models.roles.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Optional;

public class APITokenAuthorizer extends Security.Authenticator {

    @Override
    public Optional<String> getUsername(Http.Request req) {
        // Do Authentication here
        // Returning NULL means request in not authorized

        try {
            Optional APIKey = req.header("X-API-Key");

            if(APIKey.isEmpty())
            {
                return Optional.empty();
            }
            else
            {
                APIToken existingToken = APIToken.findByValue((String) APIKey.get());

                if (existingToken != null) {
                    return Optional.of(existingToken.getOwner().getUsername());
                } else {
                    return Optional.empty();
                }
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