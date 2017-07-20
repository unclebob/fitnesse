package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import fitnesse.testutil.FitNesseUtil;

import static org.junit.Assert.*;

public class DiskFileSystemTest {

  private File rootDir;

  @Before
  public void setUp() {
    rootDir = FitNesseUtil.createTemporaryFolder();
  }

  @Test
  public void findFilesAndFolders() throws IOException {
    new File(rootDir, "DirName").createNewFile();
    new File(rootDir, "filename").createNewFile();
    new File(rootDir, "CVS").createNewFile();

    List<String> listing = Arrays.asList(new DiskFileSystem().list(rootDir));

    assertEquals(listing.toString(), 2, listing.size());
    assertTrue("no DirName found", listing.contains("DirName"));
    assertTrue("no filename found", listing.contains("filename"));
  }
}
