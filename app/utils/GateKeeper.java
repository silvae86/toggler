package utils;

import models.Config;

import java.util.LinkedList;

public class GateKeeper {

    private LinkedList<Config> configs = new LinkedList<>();
    private PermissionsMap currentPermissions;

    public void addConfigChange(Config configChange) {
        configs.add(configChange);
        this.currentPermissions = new PermissionsMap();
        this.currentPermissions.apply(configChange);
    }

    public void clearConfigChanges() {
        configs.clear();
    }
}