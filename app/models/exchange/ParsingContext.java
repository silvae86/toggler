package models.exchange;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParsingContext {
    private String toggleName;
    private Boolean toggleValue;

    public ParsingContext(String toggleName, Boolean toggleValue) {
        this.toggleName = toggleName;
        this.toggleValue = toggleValue;
    }

    public ParsingContext(String toggleName) {
        this.toggleName = toggleName;
    }
}
