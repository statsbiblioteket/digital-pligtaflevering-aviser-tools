package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import org.testng.annotations.Test;

import java.io.File;

public class PromptDomsIngesterComponentTest {
    @Test(groups = "integrationTest", enabled = false)
    public void testMain() throws Exception {

        String configPath = new File(
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResource("config.properties")
                      .toURI()).getAbsolutePath();
        PromptDomsIngesterComponent.main(new String[]{configPath});

    }
}
