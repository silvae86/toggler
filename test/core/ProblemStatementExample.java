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
    public void testAddConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Toggle isButtonGreen = new Toggle("isButtonGreen");
        Toggle isButtonBlue = new Toggle("isButtonBlue");
        Toggle isButtonRed = new Toggle("isButtonRed");


        Service abc = new Service("ABC");
        Service abc100 = new Service("ABC", "1.0.0");

        Service bca = new Service("BCA");
        Service bca101 = new Service("BCA", "1.0.0");
        MongoConfig.datastore().save(bca);
        MongoConfig.datastore().save(bca101);

        Path baseConfigPath = Paths.get("test/mocks/problem_statement.yml");
        String baseConfig = baseConfigPath.toAbsolutePath().toString();

        Config config = mapper.readValue(new File(baseConfig), Config.class);
        config.apply();
        System.out.println("Applied " + baseConfigPath);
        System.out.println("\n\n" + config);

        // isButtonBlue toggle
        Assert.assertTrue(abc.canAccess(isButtonBlue));
        Assert.assertTrue(abc100.canAccess(isButtonBlue));
        Assert.assertFalse(bca.canAccess(isButtonBlue));
        Assert.assertFalse(bca101.canAccess(isButtonBlue));

        // isButtonGreen toggle
        Assert.assertTrue(abc.canAccess(isButtonGreen));
        Assert.assertTrue(abc100.canAccess(isButtonGreen));
        Assert.assertFalse(bca.canAccess(isButtonGreen));
        Assert.assertFalse(bca101.canAccess(isButtonGreen));

        // isButtonRed toggle
        Assert.assertFalse(bca.canAccess(isButtonRed));
        Assert.assertFalse(bca101.canAccess(isButtonRed));
        Assert.assertFalse(abc.canAccess(isButtonRed));
        Assert.assertFalse(abc100.canAccess(isButtonRed));
    }
}

