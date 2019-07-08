package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import models.Config;
import models.concepts.Service;
import models.concepts.Toggle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PermissionsTest {

    @Test
    public void testAddConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Path currentRelativePath = Paths.get("test/mocks/base_config.yml");
            String s = currentRelativePath.toAbsolutePath().toString();
            System.out.println("Current relative path is: " + s);

            Config config = mapper.readValue(new File(s), Config.class);
            System.out.println(ReflectionToStringBuilder.toString(config, ToStringStyle.MULTI_LINE_STYLE));
            PermissionsMap pm = new PermissionsMap();
            pm.apply(config);

            Service serviceThatShouldBeAllowed = new Service("ABC", "1.0.0");
            Toggle isButtonBlue = new Toggle("isButtonBlue");

            Assert.assertTrue(pm.canAccess(serviceThatShouldBeAllowed, isButtonBlue));


            Service serviceThatShouldBeDenied = new Service("ABC", "1.0.0");
            Toggle isButtonRed = new Toggle("isButtonRed");

            Assert.assertFalse(pm.canAccess(serviceThatShouldBeDenied, isButtonRed));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
