package auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import database.MongoConfig;
import dev.morphia.annotations.*;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import lombok.Getter;
import lombok.Setter;
import models.roles.User;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity("api_tokens")
@Indexes(
        {
                @Index(fields = {@Field("owner.username")})
        })
@Getter
@Setter
public class APIToken {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    private ObjectId id;

    @Property("date_created")
    private Instant dateCreated;

    @Property("date_expires")
    private Instant dateExpires;

    @Property("owner")
    private User owner;

    public APIToken(int validityInSeconds, User owner) {
        Instant now = Instant.now();
        this.dateCreated = now;
        this.dateExpires = now.plusSeconds(validityInSeconds);
        this.owner = owner;
    }

    public static APIToken getLastValidTokenForUser(User ownerUser) {
        Query<APIToken> lastAPITokenQuery = MongoConfig.datastore().find(APIToken.class);
        lastAPITokenQuery.and(
                lastAPITokenQuery.criteria("owner.username").equal(ownerUser.getUsername()),
                lastAPITokenQuery.criteria("date_expires").lessThan(Instant.now())
        );

        return lastAPITokenQuery.order(Sort.descending("date_created")).first();
    }
}
