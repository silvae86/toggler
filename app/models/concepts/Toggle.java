package models.concepts;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.*;

@Entity("toggleInstances")
@Indexes({
        @Index(fields = {@Field("name")}, options = @IndexOptions(unique = true, dropDups = true))
})
@Getter
@Setter
public class Toggle {

    @Property("name")
    private String name;

    public Toggle(String name) {
        this.name = name;
    }
}
