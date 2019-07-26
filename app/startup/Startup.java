package startup;

import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import models.roles.Admin;
import models.roles.User;

public class Startup {

    public Startup() {

        MongoConfig.initDatastore();

        Admin admin = Admin.findByUsernameWithRole(ConfigFactory.load().getString("username"), User.ADMIN);

        if (admin == null) {
            admin = new Admin(ConfigFactory.load().getString("admin.username"), ConfigFactory.load().getString("admin.password"));
            MongoConfig.datastore().save(admin);
        }
    }

}