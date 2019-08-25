package startup;

import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import models.roles.User;

import java.util.Collections;

public class Startup {

    public Startup() {

        MongoConfig.initDatastore();

        User normalUser = User.findByUsername(ConfigFactory.load().getString("user.username"));

        if (normalUser == null) {
            normalUser = new User(
                    ConfigFactory.load().getString("user.username"),
                    ConfigFactory.load().getString("user.password"),
                    Collections.emptyList()
            );
            MongoConfig.datastore().save(normalUser);
        }

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