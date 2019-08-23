package startup;

import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import models.roles.User;

import java.util.Collections;

public class Startup {

    public Startup() {

        MongoConfig.initDatastore();

        User admin = User.findByUsernameWithRole(ConfigFactory.load().getString("admin.username"), User.ADMIN);

        if (admin == null) {
            admin = new User(
                    ConfigFactory.load().getString("admin.username"),
                    ConfigFactory.load().getString("admin.password"),
                    Collections.singletonList(User.ADMIN)
            );
            MongoConfig.datastore().save(admin);
        }
    }

}