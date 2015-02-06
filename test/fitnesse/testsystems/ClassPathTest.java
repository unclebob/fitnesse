package fitnesse.testsystems;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClassPathTest {

  @Test
  public void shouldMergeClassPaths() {
    ClassPath cp1 = new ClassPath(asList("foo.jar", "baz.jar"), ",");
    ClassPath cp2 = new ClassPath(asList("bar.jar", "baz.jar"), "*");

    ClassPath cp = new ClassPath(asList(cp1, cp2));

    assertThat(cp.toString(), is("foo.jar,baz.jar,bar.jar"));
  }
}