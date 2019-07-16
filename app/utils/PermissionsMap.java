package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import models.Config;
import models.ConfigNode;
import models.ServiceInstance;
import models.concepts.Service;
import models.concepts.Toggle;
import play.libs.Json;

import java.util.HashSet;

@Getter
public class PermissionsMap {
    private HashSet<ServiceInstance> allowed = new HashSet<>();
    private HashSet<ServiceInstance> denied = new HashSet<>();

    private Boolean allAllowed = false;
    private Boolean allDenied = true;

    public PermissionsMap() {
    }

    public PermissionsMap(HashSet<ServiceInstance> allowed, HashSet<ServiceInstance> denied) {
        this();
        this.allowed = allowed;
        this.denied = denied;
    }

    private void mergePermissions(HashSet<ServiceInstance> allowed, HashSet<ServiceInstance> denied) {

        if (denied != null) {
            if (denied.size() == 0) {
                this.allDenied = true;
                this.denied.clear();
                if (allowed != null) {
                    this.allowed.clear();
                    this.allowed.addAll(denied);
                }
            } else {
                if (this.allowed != null)
                    this.allowed.removeAll(denied);
                this.denied.addAll(denied);
            }

            this.allAllowed = false;
        }

        if (allowed != null) {

            if (allowed.size() == 0) {
                this.allAllowed = true;
                this.allowed.clear();
                if (denied != null) {
                    this.denied.clear();
                    this.denied.addAll(denied);
                }
            } else {
                if (this.denied != null)
                    this.denied.removeAll(allowed);

                this.allowed.addAll(allowed);
            }

            this.allDenied = false;
        }
    }


    public PermissionsMap combine(PermissionsMap map) {
        HashSet<ServiceInstance> allowed = map.allowed;
        HashSet<ServiceInstance> denied = map.denied;
        mergePermissions(allowed, denied);

        return this;
    }

    public void applyHelper(ConfigNode rootNode, ConfigNode node) {
//        if(rootNode != node)
//        {
//            System.out.println("Applying node under a node for toggle " + rootNode.getName());
//        }
//        else
//        {
//            System.out.println("Applying node for toggle " + node.getName());
//        }
//
//        System.out.print(this);

        this.mergePermissions(node.getAllow(), node.getDeny());

        if (node.getOverrides() != null) {
            for (ConfigNode override : node.getOverrides()) {
                applyHelper(rootNode, override);
            }
        }

//        if(rootNode != node)
//        {
//            System.out.println("Applied node under a node for toggle " + rootNode.getName());
//        }
//        else
//        {
//            System.out.println("Applied node for toggle " + node.getName());
//        }
//
//        System.out.print("\n\n"+ this);
    }

    public PermissionsMap apply(ConfigNode node) {
        ConfigNode rootNode = node;
        this.applyHelper(rootNode, node);
        return this;
    }

    public PermissionsMap apply(HashSet<ConfigNode> nodes) {
        for (ConfigNode node : nodes) {
            this.apply(node);
        }
        return this;
    }

    public PermissionsMap apply(Config config) {
        for (ConfigNode node : config.getPermissionNodes()) {
            this.apply(node);
        }

        // System.out.println(this);

        return this;
    }


    public boolean canAccess(Service service, Toggle toggle) throws Exception {
        if (this.allDenied && this.allAllowed) {
            throw new Exception("All allowed and all denied! This is a configuration error.");
        } else if (this.allDenied) {
            for (ServiceInstance t : this.allowed) {
                if (service == t.getService()) {
                    return true;
                }
            }
            return false;
        } else if (this.allAllowed) {
            for (ServiceInstance t : this.denied) {
                if (service.equals(t.getService())) {
                    return false;
                }
            }
            return true;
        } else {
            for (ServiceInstance t : this.allowed) {
                if (service.equals(t.getService())) {
                    return true;
                }
            }

            for (ServiceInstance t : this.denied) {
                if (service.equals(t.getService())) {
                    return true;
                }
            }

            // deny by default
            return false;
        }
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String out = "";
        if (allDenied) {
            out += "All Denied " + this.allDenied + "\n";
        }

        if (allAllowed) {
            out += "All allowed " + this.allAllowed + "\n";
        }

        try {
            if (this.denied.size() > 0) {
                out += "Denied " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this.denied)) + "\n";
            }

            if (this.allowed.size() > 0) {
                out += "Allowed: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this.allowed)) + "\n";
            }
        } catch (JsonProcessingException e) {
            return ("Unable to produce a String representation of a PermissionsMap: " + e.getMessage());
        }

        return out;
    }

    public boolean getToggleValue(Service service, models.concepts.Toggle toggle) {
        return true;
    }
}
