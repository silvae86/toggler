package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("users")
public class User {
    @Id
    private ObjectId id;
    public String role;
    public String username;
    public String password;
}
