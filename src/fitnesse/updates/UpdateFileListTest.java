package fitnesse.updates;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import static util.RegexTestCase.assertSubString;
import static util.RegexTestCase.assertDoesntHaveRegexp;

import java.io.File;
import java.io.IOException;

public class UpdateFileListTest {
  private UpdateFileList updater;

  @Before
  public void setUp() {
    updater = new UpdateFileList();
  }

  @Test
  public void makeAnUpdateFileList() throws Exception {
    assertTrue(updater.getClass() == UpdateFileList.class);
  }

  @Test
  public void canParseTheCommandLine() throws Exception {
    updater.parseCommandLine(new String[]{"testDir"});
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
    String dirName = updater.getDirectories().get(0);
    assertEquals(dirName, "TestFolder");
    boolean valid = updater.directoriesAreValid();
    assertTrue(valid);
    FileUtil.deleteFileSystemDirectory(testFolder);
    valid = updater.directoriesAreValid();
    assertFalse(valid);
  }

  @Test
  public void shouldCreateAFileWithTheListOfFileNames() throws Exception {
    File testFolder = createTestFolderandFile();
    String content = runCreateFileAndGetContent(new String[]{"TestFolder"});
    assertSubString("TestFolder/TestFile", content);
    FileUtil.deleteFileSystemDirectory(testFolder);
  }


  @Test
  public void shouldMakeUpdateListWithMultiLevelFolders() throws Exception {
    File masterFolder = createMultiLevelDirectory();
    String content = runCreateFileAndGetContent(new String[]{"MasterFolder"});
    assertSubString("MasterFolder/MasterFile", content);
    assertSubString("MasterFolder/TestFolder/TestFile", content);
    FileUtil.deleteFileSystemDirectory(masterFolder);

  }

  @Test
  public void shouldKnowWhichSpecialFilesNotToInclude() throws Exception {
    File testFolder = createSpecialFileFolder();
    String content = runCreateFileAndGetContent(new String[]{"TestFolder"});
    assertSubString("TestFolder/TestFile", content);
    assertDoesntHaveRegexp("TestFolder/fitnesse.css", content);
    assertDoesntHaveRegexp("TestFolder/fitnesse_print.css", content);

    FileUtil.deleteFileSystemDirectory(testFolder);
  }

  @Test
  public void shouldPutSpecialFilesInDifferentList() throws Exception {
    File testFolder = createSpecialFileFolder();
    updater.parseCommandLine(new String[] {"TestFolder"});
    File resultFile = updater.createDoNotUpdateList();
    String content = FileUtil.getFileContent(resultFile);
    FileUtil.deleteFile(resultFile);
    assertSubString("TestFolder/fitnesse.css", content);
    assertSubString("TestFolder/fitnesse_print.css", content);
    assertDoesntHaveRegexp("TestFolder/TestFile", content);
    FileUtil.deleteFileSystemDirectory(testFolder);
  }

  private File createSpecialFileFolder() throws IOException {
    File testFolder = createTestFolderandFile();
    File specialFile = new File(testFolder, "fitnesse.css");
    specialFile.createNewFile();
    File specialFile2 = new File(testFolder, "fitnesse_print.css");
    specialFile2.createNewFile();
    return testFolder;
  }


  private String runCreateFileAndGetContent(String[] args) throws Exception {
    updater.parseCommandLine(args);
    File resultFile = updater.createUpdateList();
    String content = FileUtil.getFileContent(resultFile);
    FileUtil.deleteFile(resultFile);
    return content;
  }

  private File createTestFolderandFile() throws IOException {
    File testFolder = new File("TestFolder");
    testFolder.mkdir();
    File testFile = new File(testFolder, "TestFile");
    testFile.createNewFile();
    return testFolder;
  }

  private File createMultiLevelDirectory() throws IOException {
    File masterFolder = new File("MasterFolder");
    masterFolder.mkdir();
    File masterFile = new File(masterFolder, "MasterFile");

    masterFile.createNewFile();
    File testFolder = new File(masterFolder, "TestFolder");
    testFolder.mkdir();
    File testFile = new File(testFolder, "TestFile");
    testFile.createNewFile();
    return masterFolder;
  }
}
