package models.exchange;

import com.fasterxml.jackson.annotation.JsonAlias;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import models.roles.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;
import java.util.List;

@Entity("configurations")
@Getter
@Setter
public class Config {
    @Id
    private ObjectId id;

    @Property("toggles")
    @JsonAlias("toggles")
    private List<ConfigNode> configNodes;

    @Property("date_applied")
    private Date dateApplied;

    @Property("date_received")
    private Date dateReceived;

    @Reference("user")
    private User creator;

    public Config()
    {

    }

    public static Config getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(Config.class)
                .order("-ts").limit(1).get();
    }

    private void applyHelper(ConfigNode rootNode, ConfigNode node) {
        if(rootNode != node)
        {
            System.out.println("Applying node under a node for toggle " + rootNode.getToggleName());
        }
        else
        {
            System.out.println("Applying node for toggle " + node.getToggleName());
        }

        System.out.print(this);

        node.applyChanges();

        if (node.getOverrides() != null) {
            for (ConfigNode override : node.getOverrides()) {
                applyHelper(rootNode, override);
            }
        }

        if(rootNode != node)
        {
            System.out.println("Applied node under a node for toggle " + rootNode.getToggleName());
        }
        else
        {
            System.out.println("Applied node for toggle " + node.getToggleName());
        }

        System.out.print("\n\n"+ this);
    }

    public Config apply() {
        for (ConfigNode node: this.getConfigNodes()) {
            ConfigNode rootNode = node;
            this.applyHelper(rootNode, node);
        }

        return this;
    }
}
