package models.roles;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.Entity;

@Entity("admins")
@Getter
@Setter
public class Admin extends User{
    Admin()
    {
    }
}
