package models.database;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.*;

@Entity("toggles")
@Indexes({
        @Index(fields = {
                @Field("name"),
                @Field("service.name"),
                @Field("service.version"),
                @Field("value")
        },
        options = @IndexOptions(unique = true, dropDups = true))
})
@Getter
@Setter
public class Toggle {

    @Property("name")
    private String name;

    @Reference("service")
    private Service service;

    @Property("value")
    private Boolean value;

    public Toggle() {
    }

    public Toggle(
            String name,
            Service service,
            Boolean value
    ) {
        this.name = name;
        this.service = service;
        this.value = value;
    }


}
