package models;

import org.mongodb.morphia.annotations.Entity;

@Entity("users")
public class User {
    public String role;
    public String username;
    public String password;
}
