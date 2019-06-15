package database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.typesafe.config.ConfigFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.inject.Singleton;
import java.util.LinkedList;

@Singleton
public class MongoConfig {

    private static Datastore datastore;

    public static Datastore datastore() {
        if (datastore == null) {
            initDatastore();
        }
        return datastore;
    }

    public static void dropDatabase()
    {
        datastore().getDB().dropDatabase();
    }

    public static void initDatastore() {
        System.out.println("Initializing MongoDB Connection");
        final Morphia morphia = new Morphia();

        // Tell Morphia where to find our models
        morphia.mapPackage("models");

        ServerAddress serverAddress = new ServerAddress(ConfigFactory.load().getString("mongodb.host"), ConfigFactory.load().getInt("mongodb.port"));

        MongoCredential credential = MongoCredential.createScramSha1Credential(
                ConfigFactory.load().getString("mongodb.username"),
                ConfigFactory.load().getString("mongodb.auth_database"),
                ConfigFactory.load().getString("mongodb.password").toCharArray()
        );

        LinkedList<MongoCredential> credentials = new LinkedList<>();
        credentials.push(credential);


        MongoClient mongoClient = new MongoClient(serverAddress, credentials);

        datastore = morphia.createDatastore(
                mongoClient, ConfigFactory.load().getString("mongodb.database"));

        datastore.ensureIndexes();
        datastore.ensureCaps();
        System.out.println("MongoDB Connection Successfully Initialized.");
    }

}
