package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller contains an action to handle HTTP requests
 * to the Toggles Service
 */
public class TogglesController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    public Result delete (String id) {
        return ok(views.html.index.render());
    }

    public Result get (String id) {
        return ok(views.html.index.render());
    }

    public Result edit (String id) {
        return ok(views.html.index.render());
    }

    public Result create () {
        return ok(views.html.index.render());
    }
}
