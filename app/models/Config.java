package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity("permissions")
@Getter
@Setter
public class Config {
    @Id
    private ObjectId id;

    @Property("content")
    public String content;

    @Reference("user")
    public User creator;


    public Config()
    {

    }

    public Config(String content)
    {
        this.content = content;
    }

    public static Config getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(Config.class)
                .order("-ts").limit(1).get();
    }

}
