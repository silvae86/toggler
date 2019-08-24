package models.roles;

import auth.APIToken;
import com.typesafe.config.ConfigFactory;
import database.MongoConfig;
import dev.morphia.annotations.*;
import dev.morphia.query.Query;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.util.LinkedList;
import java.util.List;

@Entity("users")
@Getter
@Setter
public class User {
    @Id
    private ObjectId id;

    @Property
    public List<String> roles;
    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String username;
    @Property
    private String password;
    @Property
    private String salt;

    public static String USER = "user";
    public static String ADMIN = "admin";

    public User() {

    }

    public User(String username, String plainTextPassword, List<String> roles) {
        this.username = username;
        this.salt = BCrypt.gensalt(4);
        this.password = BCrypt.hashpw(plainTextPassword, this.salt);
        this.roles = roles;
    }

    public User(String username, String plainTextPassword) {
        this.username = username;
        this.salt = BCrypt.gensalt(4);
        this.password = BCrypt.hashpw(plainTextPassword, this.salt);

        LinkedList<String> userRoles = new LinkedList<>();
        userRoles.add(USER);
        this.roles = userRoles;
    }

    public static User findByUsername(String username) {
        Query<User> findByUsernameQuery = MongoConfig.datastore().find(User.class);
        findByUsernameQuery.criteria("username").equal(username);

        return findByUsernameQuery.first();
    }

    public static User findByUsernameWithRole(String username, String role) {
        Query<User> findByUsernameQuery = MongoConfig.datastore().find(User.class);
        findByUsernameQuery.and(
                findByUsernameQuery.criteria("username").equal(username),
                findByUsernameQuery.criteria("roles").equal(role)
        );

        return findByUsernameQuery.first();
    }

    public static APIToken auth(String username, String password) {
        User existingUser = findByUsername(username);

        if (existingUser == null)
            return null;
        else {
            String hashedPassword = BCrypt.hashpw(password, existingUser.salt);

            Query<User> findByUsernameAndPasswordQuery = MongoConfig.datastore().find(User.class);

            findByUsernameAndPasswordQuery.and(
                    findByUsernameAndPasswordQuery.criteria("username").equal(username),
                    findByUsernameAndPasswordQuery.criteria("password").equal(hashedPassword)
            );

            User authenticatedUser = findByUsernameAndPasswordQuery.first();
            if (authenticatedUser != null) {
                APIToken token = APIToken.getLastValidTokenForUser(authenticatedUser);
                if (token != null) {
                    return token;
                } else {
                    APIToken newToken = new APIToken(ConfigFactory.load().getInt("auth.token_validity_secs"), authenticatedUser);
                    MongoConfig.datastore().save(newToken);

                    return newToken;
                }
            } else {
                return null;
            }
        }
    }
}
