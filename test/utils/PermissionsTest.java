package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import models.Config;
import models.concepts.Service;
import models.concepts.Toggle;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class PermissionsTest {

    @Test
    public void testAddConfigs() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Path simpleConfigPath = Paths.get("test/mocks/simple_config.yml");
            String simpleConfig = simpleConfigPath.toAbsolutePath().toString();

            Config config = mapper.readValue(new File(simpleConfig), Config.class);
            PermissionsMap pm = new PermissionsMap();
            pm.apply(config);
            System.out.println("Applied " + simpleConfigPath);
            System.out.println("\n\n" + pm);

            HashSet<Service> servicesThatShouldBeAllowed = new HashSet<>();
            servicesThatShouldBeAllowed.add(new Service("ABC", "1.0.0"));
            servicesThatShouldBeAllowed.add(new Service("ABC"));

            Toggle isButtonBlue = new Toggle("isButtonBlue");

            for (Service s : servicesThatShouldBeAllowed) {
                Assert.assertTrue(pm.canAccess(s, isButtonBlue));
            }

            HashSet<Service> servicesThatShouldBeDenied = new HashSet<>();
            servicesThatShouldBeDenied.add(new Service("EG6", "1.0.1"));
            servicesThatShouldBeDenied.add(new Service("K20"));

            Toggle isButtonRed = new Toggle("isButtonRed");


            for (Service s : servicesThatShouldBeDenied) {
                Assert.assertFalse("Service " + s + " was able to access toggle " + isButtonRed + " when the spec says otherwise", pm.canAccess(s, isButtonRed));
            }


            HashSet<Service> servicesThatDontExist = new HashSet<>();
            servicesThatDontExist.add(new Service("IDONTEXIST", "1.0.1"));
            servicesThatDontExist.add(new Service("IDONTEXIST"));

            for (Service s : servicesThatDontExist) {
                Assert.assertTrue(pm.canAccess(s, isButtonRed));
            }


            Path denySomeMoreServicesConfigPath = Paths.get("test/mocks/deny_some_more_services_config.yml");
            String denySomeMoreServicesConfig = denySomeMoreServicesConfigPath.toAbsolutePath().toString();

            servicesThatShouldBeDenied.clear();
            servicesThatShouldBeAllowed.clear();

            servicesThatShouldBeDenied.add(new Service("K20"));
            servicesThatShouldBeDenied.add(new Service("EG6", "1.0.1"));
            servicesThatShouldBeAllowed.add(new Service("ABC", "1.0.3"));

            servicesThatShouldBeAllowed.add(new Service("ABC"));
            servicesThatShouldBeAllowed.add(new Service("ABC", "1.0.1"));
            servicesThatShouldBeAllowed.add(new Service("ABC", "1.0.2"));
            servicesThatShouldBeAllowed.add(new Service("K20", "1.0.2"));

            config = mapper.readValue(new File(denySomeMoreServicesConfig), Config.class);
            pm.apply(config);

            System.out.println("Applied " + denySomeMoreServicesConfigPath);
            System.out.println("\n\n" + pm);


            for (Service s : servicesThatShouldBeDenied) {
                Assert.assertFalse(pm.canAccess(s, isButtonRed));
            }

            for (Service s : servicesThatShouldBeAllowed) {
                Assert.assertTrue(pm.canAccess(s, isButtonBlue));
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
