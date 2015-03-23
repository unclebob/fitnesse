package fitnesse.updates;

import static java.util.Arrays.asList;

import org.junit.After;

import static org.junit.Assert.*;
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
  public void shouldMakeUpdateListWithMultiLevelFolders() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"MasterFolder"});
    assertSubString("MasterFolder/content.txt\n", content);
    assertSubString("MasterFolder/TestFolder/content.txt\n", content);
  }

  @Test
  public void shouldNotAddBackupAndMetadataFiles() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"MasterFolder"});
    assertDoesntHaveRegexp("MasterFolder/TestFolder/backup.zip", content);
    assertDoesntHaveRegexp("MasterFolder/TestFolder/.DS_Store", content);
  }

  @Test
  public void shouldKnowWhichSpecialFilesNotToInclude() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/content.txt";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/properties.xml";
    String content = runCreateFileAndGetContent(new String[]{arg1, arg2, "MasterFolder/TestFolder"});
    assertDoesntHaveRegexp("TestFolder/content.txt", content);
    assertDoesntHaveRegexp("TestFolder/properties.xml", content);
  }

  @Test
  public void shouldPutSpecialFilesInDifferentList() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/content.txt";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/properties.xml";
    updater.parseCommandLine(new String[]{arg1, arg2, "MasterFolder/TestFolder"});
    File doNotUpdateFile = updater.createDoNotUpdateList();
    String doNotUpdateContent = FileUtil.getFileContent(doNotUpdateFile);
    FileUtil.deleteFile(doNotUpdateFile);
    assertSubString("TestFolder/content.txt", doNotUpdateContent);
    assertSubString("TestFolder/properties.xml", doNotUpdateContent);
  }

  @Test
  public void shouldPrunePrefixes() throws Exception {
    String content = runCreateFileAndGetContent(new String[]{"-baseDirectory:MasterFolder/TestFolder", ""});
    assertSubString("content.txt\n", content);
    assertDoesntHaveRegexp("TestFolder/content.txt", content);
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
    verify(updaterMock).exit();
  }

  @Test
  public void shouldSplitUpWindowsLikePathNames() throws Exception {
    String args[] = {"-baseDirectory:C:\\FitNesse/Resources", "MasterFolder"};
    updater.parseCommandLine(args);
    assertEquals(asList("C:\\FitNesse/Resources/MasterFolder"), updater.getDirectories());
 
  }

  private String runCreateFileAndGetContent(String[] args) throws Exception {
    updater.parseCommandLine(args);
    File resultFile = updater.createUpdateList();
    String content = FileUtil.getFileContent(resultFile);
    FileUtil.deleteFile(resultFile);
    return content;
  }

  private void createMultiLevelDirectory() throws IOException {
    FileUtil.createFile("MasterFolder/content.txt", "");
    FileUtil.createFile("MasterFolder/TestFolder/content.txt", "");
    FileUtil.createFile("MasterFolder/TestFolder/properties.xml", "");
    FileUtil.createFile("MasterFolder/TestFolder/backup.zip", "");
    FileUtil.createFile("MasterFolder/TestFolder/.DS_Store", "");
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("MasterFolder");
  }
}
