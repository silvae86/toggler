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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(JUnit4.class)
public class AllowAllExceptAllVersionsOfOneService {

    @Before
    public void init() {
        MongoConfig.dropDatabase();
    }

    @Test
    public void testAddConfig() throws IOException, Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        Path baseConfigPath = Paths.get("test/mocks/allow_all_except_one.yml");
        String baseConfig = baseConfigPath.toAbsolutePath().toString();

        Config config = mapper.readValue(new File(baseConfig), Config.class);
        config.apply();
        System.out.println("Applied " + baseConfigPath);
        System.out.println("\n\n" + config);

        Toggle isButtonGreen = new Toggle("isButtonGreen", true);

        // CDE should access
        Service cde = new Service("CDE");
        Assert.assertTrue(cde.canAccess(isButtonGreen));

        Service cde100 = new Service("CDE", "1.0.0");
        Assert.assertTrue(cde100.canAccess(isButtonGreen));

        // BCA should access
        Service bca = new Service("BCA");
        Assert.assertTrue(bca.canAccess(isButtonGreen));

        Service bca100 = new Service("BCA", "1.0.0");
        Assert.assertTrue(bca100.canAccess(isButtonGreen));

        // ABC without version should access
        Service abc = new Service("ABC");
        Assert.assertTrue(abc.canAccess(isButtonGreen));

        // ABC 1.0.0 denied
        Service abc100 = new Service("ABC", "1.0.0");
        Assert.assertFalse(abc100.canAccess(isButtonGreen));

        // ABC 1.0.1 denied
        Service abc101 = new Service("ABC", "1.0.1");
        Assert.assertFalse(abc101.canAccess(isButtonGreen));
    }
}
