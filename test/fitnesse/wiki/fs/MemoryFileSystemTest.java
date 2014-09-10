package fitnesse.wiki.fs;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.fs.MemoryFileSystem;
import org.junit.Before;
import org.junit.Test;

public class MemoryFileSystemTest {

  private static final File pageOne = new File("PageOne");
  private static final File contentFile = new File(pageOne, "content.txt");

  MemoryFileSystem fileSystem;

  @Before
  public void setUp() {
    fileSystem = new MemoryFileSystem();

    fileSystem.makeDirectory(pageOne);
    fileSystem.makeFile(contentFile, "content");
  }

  @Test
  public void shouldAddPages() {
    assertTrue(fileSystem.exists(pageOne));
    assertTrue(fileSystem.exists(contentFile));
  }

  @Test
  public void shouldDeletePages() {
    fileSystem.delete(pageOne);

    assertFalse(fileSystem.exists(pageOne));
    assertFalse(fileSystem.exists(contentFile));
  }
}
