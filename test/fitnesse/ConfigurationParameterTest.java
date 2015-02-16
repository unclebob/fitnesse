package fitnesse;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ConfigurationParameterTest {

  @Test
  public void shouldMakePropertiesWithConfigurationParametersAndValues() {
    Properties properties = ConfigurationParameter.makeProperties(ConfigurationParameter.PORT, 8001,
            ConfigurationParameter.ROOT_PATH, ".",
            ConfigurationParameter.ROOT_DIRECTORY, "FitNesseRoot");

    assertThat(properties.getProperty(ConfigurationParameter.PORT.getKey()), is("8001"));
    assertThat(properties.getProperty(ConfigurationParameter.ROOT_PATH.getKey()), is("."));
    assertThat(properties.getProperty(ConfigurationParameter.ROOT_DIRECTORY.getKey()), is("FitNesseRoot"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailOnOddNumberOfArguments() {
    ConfigurationParameter.makeProperties(ConfigurationParameter.PORT, 8001,
            ConfigurationParameter.ROOT_PATH);
  }

  @Test
  public void shouldAcceptStringArgumentsInsteadOfConfigurationParameters() {
    Properties properties = ConfigurationParameter.makeProperties("Port", 8001);

    assertThat(properties.getProperty("Port"), is("8001"));
  }

  @Test
  public void shouldAcceptNuArguments() {
    Properties properties = ConfigurationParameter.makeProperties();

    assertThat(properties, is(notNullValue()));
  }

  @Test
  public void canLoadPropertiesFromFile() {
    Properties properties = ConfigurationParameter.loadProperties(new File("configuration-parameter-test.properties"));

    assertThat(properties.getProperty("unitTestProperty"), is("found"));
  }

}
