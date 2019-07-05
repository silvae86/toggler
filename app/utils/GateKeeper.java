package utils;

import models.ConfigChange;
import models.ServiceToggle;
import models.ToggleInstance;
import models.concepts.Service;
import models.concepts.Toggle;

import java.util.HashSet;
import java.util.LinkedList;

public class GateKeeper {

    private LinkedList<ConfigChange> configs = new LinkedList<>();

    public Service applyChange(Service start, ConfigChange delta) {
        for (ToggleInstance t : delta.getToggles()) {
            boolean defaultValue = t.getDefaultValue();

            HashSet<ServiceToggle> allowedServices = getAllowedServices(t);
            HashSet<ServiceToggle> deniedServices = getDeniedServices(t);
        }
    }

    public void addConfigChange(ConfigChange configChange) {
        configs.add(configChange);
    }

    public boolean canAccess(Service service, Toggle toggle) {
        return true;
    }

    public boolean toggleValue(Service service, Toggle toggle) {
        return true;
    }

    public void clearConfigChanges() {
        configs.clear();
    }
}