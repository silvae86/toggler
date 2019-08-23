package auth;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Optional;

public class JWTAuthorizer extends Security.Authenticator {

    @Override
    public Optional<String> getUsername(Http.Request req) {
        // Do Authentication here
        // Returning NULL means request in not authorized


        return null;
    }

    @Override
    public Result onUnauthorized(Http.Request req) {
        // Response for unauthenticated request
        return unauthorized("Invalid API Token");
    }
}