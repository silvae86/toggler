package models;

import org.mongodb.morphia.annotations.Entity;

@Entity("admins")
public class Admin extends User{
    Admin()
    {
    }
}
