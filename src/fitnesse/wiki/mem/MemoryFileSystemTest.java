package fitnesse.wiki.mem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MemoryFileSystemTest {

  MemoryFileSystem fileSystem;

  @Before
  public void setUp() {
    fileSystem = new MemoryFileSystem();

    fileSystem.makeDirectory("PageOne");
    fileSystem.makeFile("PageOne/content.txt", "content");
  }

  @Test
  public void shouldAddPages() {
    assertTrue(fileSystem.exists("PageOne"));
    assertTrue(fileSystem.exists("PageOne/content.txt"));
  }

  @Test
  public void shouldDeletePages() {
    fileSystem.delete("PageOne");

    assertFalse(fileSystem.exists("PageOne"));
    assertFalse(fileSystem.exists("PageOne/content.txt"));
  }
}
