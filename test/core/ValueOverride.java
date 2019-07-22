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
public class ValueOverride {

    @Before
    public void init() {
        MongoConfig.dropDatabase();
    }

    @Test
    public void testAddConfig() throws IOException, Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path baseConfigPath = Paths.get("test/mocks/value_override.yml");
        String baseConfig = baseConfigPath.toAbsolutePath().toString();

        Config config = mapper.readValue(new File(baseConfig), Config.class);
        config.apply();
        System.out.println("Applied " + baseConfigPath);
        System.out.println("\n\n" + config);

        Toggle isButtonGreen = new Toggle("isButtonGreen");

        Service abc = new Service("ABC");
        Assert.assertEquals("true", abc.getToggleValue(isButtonGreen).toString());
    }
}
