package fitnesse;

import java.io.IOException;

import fitnesse.plugins.PluginException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContextConfiguratorTest {

  @Test
  public void canSetContextRoot() throws IOException, PluginException {
    ContextConfigurator configurator = ContextConfigurator.empty();

    configurator.withParameter(ConfigurationParameter.ROOT_PATH, ".");
    configurator.withParameter(ConfigurationParameter.CONTEXT_ROOT, "fitnesse/");

    String contextRoot = configurator.makeFitNesseContext().contextRoot;

    assertThat(contextRoot, is("/fitnesse/"));
  }

  @Test
  public void shouldSetTrailingSlashOnContextRoot() throws IOException, PluginException {
    ContextConfigurator configurator = ContextConfigurator.empty();

    configurator.withParameter(ConfigurationParameter.ROOT_PATH, ".");
    configurator.withParameter(ConfigurationParameter.CONTEXT_ROOT, "fitnesse");

    String contextRoot = configurator.makeFitNesseContext().contextRoot;

    assertThat(contextRoot, is("/fitnesse/"));
  }

}
