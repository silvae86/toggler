package models.roles;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.HashSet;

@Entity("users")
@Getter
@Setter
public class User {
    @Id
    private ObjectId id;
    private String username;
    private String password;
    private String salt;
    public HashSet<String> roles;

    public boolean auth(String password)
    {
        String hashedPassword = BCrypt.hashpw(password, this.salt);
        return hashedPassword.equals(this.password);
    }

    public User (String username, String plainTextPassword)
    {
        this.username = username;
        this.salt = BCrypt.gensalt(12);
        this.password = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public User()
    {

    }
}
