package fitnesse.wiki.fs;

import java.io.File;
import java.util.Properties;

import fitnesse.components.ComponentFactory;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;

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


  @Test
  public void testSetAttributes() throws Exception {
    File rootPath = FitNesseUtil.createTemporaryFolder();
    SimpleFileVersionsController versionsController = new SimpleFileVersionsController();
    FileSystemPageFactory fileSystemPageFactory = new FileSystemPageFactory(new DiskFileSystem(), versionsController);
    WikiPage root = fileSystemPageFactory.makePage(rootPath, "RooT", null, new SystemVariableSource());

    PageData data = root.getData();
    data.setAttribute("Test", "true");
    data.setAttribute("Search", "true");
    root.commit(data);

    assertTrue(root.getData().hasAttribute("Test"));
    assertTrue(root.getData().hasAttribute("Search"));
    assertEquals("", root.getData().getAttribute("Test"));
  }

}
