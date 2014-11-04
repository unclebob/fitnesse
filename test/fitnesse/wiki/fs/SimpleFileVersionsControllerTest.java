package fitnesse.wiki.fs;

import java.util.Properties;

import fitnesse.components.ComponentFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class SimpleFileVersionsControllerTest {

  @Test
  public void canCreateVersionsControllerThroughComponentFactory() {
    Properties properties = new Properties();
    properties.put("VersionsController", "fitnesse.wiki.fs.SimpleFileVersionsController");
    ComponentFactory componentFactory = new ComponentFactory(properties);
    Object versionsController = componentFactory.createComponent("VersionsController", null);

    assertThat(versionsController, instanceOf(SimpleFileVersionsController.class));
  }
}