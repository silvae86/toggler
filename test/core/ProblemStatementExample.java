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
public class ProblemStatementExample {

    @Before
    public void init() {
        MongoConfig.dropDatabase();
    }

    @Test
    public void testAddConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Path baseConfigPath = Paths.get("test/mocks/problem_statement.yml");
            String baseConfig = baseConfigPath.toAbsolutePath().toString();

            Config config = mapper.readValue(new File(baseConfig), Config.class);
            config.apply();
            System.out.println("Applied " + baseConfigPath);
            System.out.println("\n\n" + config);

            Toggle isButtonGreen = new Toggle("isButtonGreen", true);

            Service abc = new Service("ABC");
            Assert.assertFalse(abc.canAccess(isButtonGreen));

            Service abc100 = new Service("ABC", "1.0.0");
            Assert.assertFalse(abc100.canAccess(isButtonGreen));

            Service bca = new Service("BCA");
            Assert.assertTrue(bca.canAccess(isButtonGreen));

            Service bca101 = new Service("BCA", "1.0.0");
            Assert.assertTrue(bca101.canAccess(isButtonGreen));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
