package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import models.exchange.Config;
import models.database.Service;
import models.database.Toggle;
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
            config.apply();
            System.out.println("Applied " + simpleConfigPath);
            System.out.println("\n\n" + config);

            Service abc = new Service("ABC");
            Service abc100 = new Service("ABC", "1.0.0");
            Service abc101 = new Service("ABC", "1.0.1");
            Service abc102 = new Service("ABC", "1.0.2");
            Service abc103 = new Service("ABC", "1.0.3");

            Service eg6101 = new Service("EG6", "1.0.1");
            Service k20 = new Service("K20");
            Service k20103 = new Service("K20", "1.0.3");

            Service iDontExist101new = new Service("IDONTEXIST", "1.0.1");
            Service iDontExist = new Service("IDONTEXIST");

            HashSet<Service> servicesThatShouldBeAllowed = new HashSet<>();

            servicesThatShouldBeAllowed.add(abc100);
            servicesThatShouldBeAllowed.add(abc);

            Toggle isButtonBlue = new Toggle("isButtonBlue", abc, true);

            for (Service s : servicesThatShouldBeAllowed) {
                Assert.assertTrue(s.canAccess(isButtonBlue));
            }

            HashSet<Service> servicesThatShouldBeDenied = new HashSet<>();
            servicesThatShouldBeDenied.add(eg6101);
            servicesThatShouldBeDenied.add(k20);

            Toggle isButtonRed = new Toggle("isButtonBlue", abc, true);


            for (Service s : servicesThatShouldBeDenied) {
                Assert.assertFalse("Service " + s + " was able to access toggle " + isButtonRed + " when the spec says otherwise", s.canAccess(isButtonRed));
            }


            HashSet<Service> servicesThatDontExist = new HashSet<>();
            servicesThatDontExist.add(iDontExist);
            servicesThatDontExist.add(iDontExist101new);

            for (Service s : servicesThatDontExist) {
                Assert.assertTrue(s.canAccess(isButtonRed));
            }

            Path denySomeMoreServicesConfigPath = Paths.get("test/mocks/deny_some_more_services_config.yml");
            String denySomeMoreServicesConfig = denySomeMoreServicesConfigPath.toAbsolutePath().toString();

            servicesThatShouldBeDenied.clear();
            servicesThatShouldBeAllowed.clear();

            servicesThatShouldBeDenied.add(k20);
            servicesThatShouldBeDenied.add(eg6101);

            servicesThatShouldBeAllowed.add(abc);
            servicesThatShouldBeAllowed.add(abc101);
            servicesThatShouldBeAllowed.add(abc102);
            servicesThatShouldBeAllowed.add(abc103);
            servicesThatShouldBeAllowed.add(k20103);

            config = mapper.readValue(new File(denySomeMoreServicesConfig), Config.class);
            config.apply();

            System.out.println("Applied " + denySomeMoreServicesConfigPath);
            System.out.println("\n\n" + config);


            for (Service s : servicesThatShouldBeDenied) {
                Assert.assertFalse(s.canAccess(isButtonRed));
            }

            for (Service s : servicesThatShouldBeAllowed) {
                Assert.assertTrue(s.canAccess(isButtonBlue));
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
