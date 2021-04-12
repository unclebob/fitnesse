package fitnesse;

import fitnesse.plugins.PluginException;
import fitnesse.plugins.PluginsLoaderTest;
import fitnesse.util.ClassUtils;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContextConfiguratorTest {
  private final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

  @After
  public void tearDown() {
    ClassUtils.setClassLoader(null);
    Thread.currentThread().setContextClassLoader(originalClassLoader);
  }

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

  @Test
  public void bootstrapIsDefaultTheme() throws IOException, PluginException {
    String theme = ContextConfigurator
      .empty()
      .makeFitNesseContext()
      .theme;

    assertThat(theme, is("bootstrap"));
  }

  @Test
  public void themeCanBeSetViaProperties() throws IOException, PluginException {
    String theme = ContextConfigurator
      .empty()
      .withParameter(ConfigurationParameter.THEME, "othertheme")
      .makeFitNesseContext()
      .theme;

    assertThat(theme, is("othertheme"));
  }

  @Test
  public void themeCanBeSetByPlugin() throws IOException, PluginException {
    String theme = ContextConfigurator
      .empty()
      .withClassLoader(PluginsLoaderTest.createClassLoaderWithTestPlugin())
      .makeFitNesseContext()
      .theme;

    assertThat(theme, is("dummy-theme"));
  }

  @Test
  public void themeInPropertiesBeatsOneInPlugin() throws IOException, PluginException {
    String theme = ContextConfigurator
      .empty()
      .withParameter(ConfigurationParameter.THEME, "othertheme")
      .withClassLoader(PluginsLoaderTest.createClassLoaderWithTestPlugin())
      .makeFitNesseContext()
      .theme;

    assertThat(theme, is("othertheme"));
  }

}
