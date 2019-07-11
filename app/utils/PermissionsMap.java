package utils;

import lombok.Getter;
import models.Config;
import models.ConfigNode;
import models.ServiceInstance;
import models.concepts.Service;
import models.concepts.Toggle;

import java.util.HashSet;

@Getter
public class PermissionsMap {
    private HashSet<ServiceInstance> allowed = new HashSet<>();
    private HashSet<ServiceInstance> denied = new HashSet<>();

    private Boolean allAllowed = false;
    private Boolean allDenied = true;

    public PermissionsMap() {
    }

<<<<<<< HEAD
    public PermissionsMap(HashSet<Toggle> allowed, HashSet<Toggle> denied) {
        this.allowed = allowed;
=======
    public PermissionsMap(HashSet<ServiceInstance> allowed, HashSet<ServiceInstance> denied) {
        this();
        this.allowed = allowed; 
>>>>>>> 807108676cac8fe5bebe2b263995dafff6f134f3
        this.denied = denied;
    }

    private void mergePermissions(HashSet<ServiceInstance> allowed, HashSet<ServiceInstance> denied) {
        if (allowed != null) {
            if (allowed.size() > 0) {
                this.allowed.addAll(allowed);
                this.denied.removeAll(denied);
            } else {
                this.allAllowed = true;
                this.allDenied = false;
            }
        }

        if (denied != null) {
            if (denied.size() > 0) {
                this.denied.addAll(denied);
                this.allowed.removeAll(denied);
            } else {
                this.allAllowed = false;
                this.allDenied = true;
            }
        }
    }

    public PermissionsMap combine(PermissionsMap map) {
        HashSet<ServiceInstance> allowed = map.allowed;
        HashSet<ServiceInstance> denied = map.denied;
        mergePermissions(allowed, denied);

        return this;
    }

    public PermissionsMap apply(Config change) {
        this.apply(change.getPermissionNodes());
        return this;
    }

    public PermissionsMap apply(HashSet<ConfigNode> nodes) {
        for (ConfigNode node : nodes) {
            this.apply(node);
        }

        return this;
    }

    public PermissionsMap apply(ConfigNode node) {
        HashSet<ServiceInstance> allowed = node.getAllow();
        HashSet<ServiceInstance> denied = node.getDeny();

        this.mergePermissions(allowed, denied);
        return this;
    }

    public boolean canAccess(Service service, Toggle toggle) throws Exception {
        if (this.allDenied && this.allAllowed) {
            throw new Exception("All allowed and all denied! This is a configuration error.");
        } else if (this.allDenied) {
            return false;
        } else if (this.allAllowed) {
            return true;
        } else {
            for (ServiceInstance t : this.allowed) {
                if (service == t.getService()) {
                    return true;
                }
            }

            for (ServiceInstance t : this.denied) {
                if (service == t.getService()) {
                    return true;
                }
            }

            // deny by default
            return false;
        }
    }

    public boolean getToggleValue(Service service, models.concepts.Toggle toggle) {
        return true;
    }
}
