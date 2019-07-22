package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import database.MongoConfig;
import models.database.Service;
import models.database.Toggle;
import models.exchange.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(JUnit4.class)
public class DenyAllExceptAllVersionsOfOneService {

    @Before
    public void init() {
        MongoConfig.dropDatabase();
    }

    @Test
    public void testAddConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Path baseConfigPath = Paths.get("test/mocks/deny_all_except_one.yml");
            String baseConfig = baseConfigPath.toAbsolutePath().toString();

            Config config = mapper.readValue(new File(baseConfig), Config.class);
            config.apply();
            System.out.println("Applied " + baseConfigPath);
            System.out.println("\n\n" + config);

            Toggle isButtonRed = new Toggle("isButtonRed", true);

            Service abc = new Service("ABC");
            Assert.assertTrue(abc.canAccess(isButtonRed));

            Service abc100 = new Service("ABC", "1.0.0");
            Assert.assertTrue(abc100.canAccess(isButtonRed));

            Service abc101 = new Service("ABC", "1.0.1");
            Assert.assertTrue(abc101.canAccess(isButtonRed));

            Service abc102 = new Service("ABC", "1.0.2");
            Assert.assertTrue(abc102.canAccess(isButtonRed));

            Service abc103 = new Service("ABC", "1.0.3");
            Assert.assertTrue(abc103.canAccess(isButtonRed));

            Service eg6101 = new Service("EG6", "1.0.1");
            Assert.assertFalse(eg6101.canAccess(isButtonRed));

            Service k20 = new Service("K20");
            Assert.assertFalse(k20.canAccess(isButtonRed));

            Service k20103 = new Service("K20", "1.0.3");
            Assert.assertFalse(k20103.canAccess(isButtonRed));

            Service iDontExist101new = new Service("IDONTEXIST", "1.0.1");
            Assert.assertFalse(iDontExist101new.canAccess(isButtonRed));

            Service iDontExist = new Service("IDONTEXIST");
            Assert.assertFalse(iDontExist.canAccess(isButtonRed));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
