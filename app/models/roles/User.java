package models.roles;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Entity("users")
@Getter
@Setter
public class User {
    @Id
    private ObjectId id;
    private String username;
    private String password;
    private String salt;
    public List<String> roles;

    public User(String username, String plainTextPassword) {
        this.username = username;
        this.salt = BCrypt.gensalt(12);
        this.password = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public User() {

    }

    public boolean auth(String password) {
        String hashedPassword = BCrypt.hashpw(password, this.salt);
        return hashedPassword.equals(this.password);
    }
}
