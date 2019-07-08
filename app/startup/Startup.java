package startup;

import database.MongoConfig;

public class Startup {

    public Startup() {

        MongoConfig.initDatastore();

    }

}