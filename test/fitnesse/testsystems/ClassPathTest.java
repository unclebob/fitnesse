package fitnesse.testsystems;

import java.util.Collections;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClassPathTest {

  @Test
  public void shouldMergeClassPaths() {
    ClassPath cp1 = new ClassPath(asList("foo.jar", "baz.jar"), ",");
    ClassPath cp2 = new ClassPath(asList("bar.jar", "baz.jar"), "*");

    ClassPath cp = new ClassPath(asList(cp1, cp2));

    assertThat(cp.toString(), is("foo.jar,baz.jar,bar.jar"));
  }

  @Test
  public void lookupJarForClassFromDirecotry() {
    String mainClass = "fitnesse.slim.SlimService";
    ClassPath path = new ClassPath(Collections.<String>emptyList(), ";").withLocationForClass(mainClass);
    String userDir = System.getProperty("user.dir");
    assertTrue(String.format("Paths '%s' and '%s' are not identical", path, userDir), path.toString().toLowerCase().startsWith(userDir.toLowerCase()));
  }

  @Test
  public void lookupJarForClassFromJar() {
    String mainClass = "org.apache.velocity.app.VelocityEngine";
    ClassPath path = new ClassPath(Collections.<String>emptyList(), ";").withLocationForClass(mainClass);
    assertTrue("Lookup did not resolve to the right jar file: " + path, path.toString().matches(".*[\\\\/]velocity.*\\.jar"));
  }

}