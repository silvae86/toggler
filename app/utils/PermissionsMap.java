package utils;

import lombok.Getter;
import models.concepts.Service;
import models.concepts.Toggle;

import java.util.HashSet;

@Getter
public class PermissionsMap {
    private HashSet<Service> allowed;
    private HashSet<Service> denied;

    private Boolean allAllowed = false;
    private Boolean allDenied = true;

    public PermissionsMap() {

    }

    public PermissionsMap(HashSet<Service> allowed, HashSet<Service> denied) {
        this.allowed = allowed; 
        this.denied = denied;
    }

    public PermissionsMap combine(PermissionsMap map) {
        HashSet<Service> allowed = map.allowed;
        HashSet<Service> denied = map.denied;

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
            for (Service t : this.allowed) {
                if (service == t) {
                    return true;
                }
            }

            for (Service t : this.denied) {
                if (service == t) {
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
