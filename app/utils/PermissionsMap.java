package utils;

import lombok.Getter;
import models.Toggle;
import models.concepts.Service;

import java.util.HashSet;

@Getter
public class PermissionsMap {
    private HashSet<Toggle> allowed;
    private HashSet<Toggle> denied;

    private Boolean allAllowed = false;
    private Boolean allDenied = true;

    public PermissionsMap() {

    }

    public PermissionsMap(HashSet<Toggle> allowed, HashSet<Toggle> denied) {
        this.allowed = allowed;
        this.denied = denied;
    }

    ;

    public PermissionsMap combine(PermissionsMap map) {
        HashSet<Toggle> allowed = map.allowed;
        HashSet<Toggle> denied = map.denied;

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
            throw new Exception("All allowed and all denied!");
        } else if (this.allDenied) {
            return false;
        } else if (this.allAllowed) {
            return true;
        } else {
            for (Toggle t : this.allowed) {
                // TODO I was here.
                if (t.getAppliesTo() == service) {
                    return true;
                }
            }

            for (Toggle t : this.denied) {
                if (t.getAppliesTo() == service) {
                    return false;
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
