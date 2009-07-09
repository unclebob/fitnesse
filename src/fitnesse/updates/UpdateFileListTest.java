package fitnesse.updates;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import util.FileUtil;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertSubString;

import java.io.File;
import java.io.IOException;

public class UpdateFileListTest {
  private UpdateFileList updater;

  @Before
  public void setUp() throws IOException {
    updater = new UpdateFileList();
    createMultiLevelDirectory();
  }

  @Test
  public void makeAnUpdateFileList() throws Exception {
    assertTrue(updater.getClass() == UpdateFileList.class);
  }

  @Test
  public void canParseTheCommandLine() throws Exception {
    updater.parseCommandLine(new String[]{"testDir"});
    assertEquals(1, updater.getDirectories().size());
    assertEquals("testDir", updater.getDirectories().get(0));
  }

  @Test
  public void shouldHandleInvalidCommandLine() throws Exception {
    boolean validCommandLine = updater.parseCommandLine(new String[0]);
    assertFalse(validCommandLine);
  }

  @Test
  public void shouldKnowIfAGivenDirectoryExists() throws Exception {
    File testFolder = new File("TestFolder");
    testFolder.mkdir();
    updater.parseCommandLine(new String[]{"TestFolder"});
    assertTrue(updater.directoriesAreValid());
    FileUtil.deleteFileSystemDirectory(testFolder);
    assertFalse(updater.directoriesAreValid());
  }

  @Test
  public void shouldCreateAFileWithTheListOfFileNames() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"MasterFolder"});
    assertSubString("MasterFolder/MasterFile\n", content);
  }

  @Test
  public void shouldMakeUpdateListWithMultiLevelFolders() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"MasterFolder"});
    assertSubString("MasterFolder/MasterFile\n", content);
    assertSubString("MasterFolder/TestFolder/TestFile\n", content);
  }

  @Test
  public void shouldKnowWhichSpecialFilesNotToInclude() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/fitnesse.css";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/fitnesse_print.css";
    String content = runCreateFileAndGetContent(new String[]{arg1, arg2, "MasterFolder/TestFolder"});
    assertSubString("TestFolder/TestFile", content);
    assertDoesntHaveRegexp("TestFolder/fitnesse.css", content);
    assertDoesntHaveRegexp("TestFolder/fitnesse_print.css", content);
  }

  @Test
  public void shouldPutSpecialFilesInDifferentList() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/fitnesse.css";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/fitnesse_print.css";
    updater.parseCommandLine(new String[]{arg1, arg2, "MasterFolder/TestFolder"});
    File doNotUpdateFile = updater.createDoNotUpdateList();
    String doNotUpdateContent = FileUtil.getFileContent(doNotUpdateFile);
    FileUtil.deleteFile(doNotUpdateFile);
    assertSubString("TestFolder/fitnesse.css", doNotUpdateContent);
    assertSubString("TestFolder/fitnesse_print.css", doNotUpdateContent);
    assertDoesntHaveRegexp("TestFolder/TestFile", doNotUpdateContent);
  }

  @Test
  public void shouldPrunePrefixes() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"-baseDirectory:MasterFolder/TestFolder", ""});
    assertSubString("TestFile\n", content);
    assertDoesntHaveRegexp("TestFolder/TestFile", content);
  }

  @Test
  public void testMainHappyPath() throws Exception {
    String args[] = {"foo", "bar"};
    UpdateFileList updaterMock = mock(UpdateFileList.class);
    UpdateFileList.testUpdater = updaterMock;
    when(updaterMock.directoriesAreValid()).thenReturn(true);
    UpdateFileList.main(args);
    verify(updaterMock).parseCommandLine(args);
    verify(updaterMock).createUpdateList();
    verify(updaterMock).createDoNotUpdateList();
  }

  @Test
  public void testMainUnhappyPath() throws Exception {
    String args[] = {"foo", "bar"};
    UpdateFileList updaterMock = mock(UpdateFileList.class);
    UpdateFileList.testUpdater = updaterMock;
    when(updaterMock.directoriesAreValid()).thenReturn(false);
    UpdateFileList.main(args);
    verify(updaterMock).printMessage("Some directories are invalid.");
    verify(updaterMock).exit();
  }


  private String runCreateFileAndGetContent(String[] args) throws Exception {
    updater.parseCommandLine(args);
    File resultFile = updater.createUpdateList();
    String content = FileUtil.getFileContent(resultFile);
    FileUtil.deleteFile(resultFile);
    return content;
  }

  private void createMultiLevelDirectory() throws IOException {
    FileUtil.createFile("MasterFolder/MasterFile", "");
    FileUtil.createFile("MasterFolder/TestFolder/TestFile", "");
    FileUtil.createFile("MasterFolder/TestFolder/fitnesse.css", "");
    FileUtil.createFile("MasterFolder/TestFolder/fitnesse_print.css", "");
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("MasterFolder");
  }
}
