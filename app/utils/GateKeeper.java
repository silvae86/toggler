package utils;

import models.ConfigChange;

import java.util.LinkedList;

public class GateKeeper {

    private LinkedList<ConfigChange> configs = new LinkedList<>();
    private PermissionsMap currentPermissions;

    public void addConfigChange(ConfigChange configChange) {
        configs.add(configChange);
        this.currentPermissions = configChange.getPermissions();
    }

    public void clearConfigChanges() {
        configs.clear();
    }
}