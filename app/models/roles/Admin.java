package models.roles;

import dev.morphia.annotations.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity("admins")
@Getter
@Setter
public class Admin extends User {
    Admin() {
    }
}
