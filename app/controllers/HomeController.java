package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */


public class HomeController extends Controller {
    public Result index() {
        return redirect("/docs/swagger-ui/index.html?url=/assets/swagger.json");
    }

}
