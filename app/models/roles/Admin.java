package models.roles;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Entity("admins")
@Getter
@Setter
public class Admin extends User {

    @Id
    private ObjectId id;
    @Property
    private String username;
    @Property
    private String password;
    @Property
    private String salt;

    public Admin() {
    }

    public Admin(String username, String plainTextPassword) {
        super(username, plainTextPassword);
    }
}
