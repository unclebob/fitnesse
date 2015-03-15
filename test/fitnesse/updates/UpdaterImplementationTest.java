package fitnesse.updates;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertSubString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class UpdaterImplementationTest {
  private File updateList;
  private File updateDoNotCopyOver;
  public static final String testDir = "testDir";
  public static final String rootName = "RooT";

  protected WikiPage root;
  protected UpdaterImplementation updater;
  protected WikiPage pageOne;
  protected WikiPage pageTwo;
  protected FitNesseContext context;
  private boolean updateDone = false;

  @Before
  public void setUp() throws Exception {
    setTheContext(rootName);
    root = setTheRoot();
    createFakeJarFileResources();
    createFakeUpdateListFiles();
    updater = new UpdaterImplementation(context);
  }

  private WikiPage setTheRoot() throws Exception {
    return root;
  }

  private void setTheContext(String name) {
    FileUtil.makeDir(testDir);
    context = FitNesseUtil.makeTestContext(new FileSystemPageFactory(), testDir, name, 80);
    root = context.getRootPage();
    root.commit(root.getData());
  }

  private void createFakeUpdateListFiles() {
    updateList = new File("classes/Resources/updateList");
    updateDoNotCopyOver = new File("classes/Resources/updateDoNotCopyOverList");
    FileUtil.createFile(updateList, "FitNesseRoot/files/TestFile\nFitNesseRoot/files/BestFile\n");
    FileUtil.createFile(updateDoNotCopyOver, "FitNesseRoot/SpecialFile");
  }

  private void createFakeJarFileResources() throws IOException {
    FileUtil.createFile("classes/Resources/FitNesseRoot/files/TestFile","") ;
    FileUtil.createFile("classes/Resources/FitNesseRoot/files/BestFile","") ;
    FileUtil.createFile("classes/Resources/FitNesseRoot/SpecialFile","");
  }

  @Test
  public void shouldBeAbleToGetUpdateFilesAndMakeAlistFromThem() throws Exception {
    ArrayList<String> updateArrayList = new ArrayList<String>();
    updater.tryToParseTheFileIntoTheList(updateList, updateArrayList);
    assertEquals("FitNesseRoot/files/TestFile", updateArrayList.get(0));
    assertEquals("FitNesseRoot/files/BestFile", updateArrayList.get(1));
    updateArrayList = new ArrayList<String>();
    updater.tryToParseTheFileIntoTheList(updateDoNotCopyOver, updateArrayList);
    assertEquals("FitNesseRoot/SpecialFile", updateArrayList.get(0));
  }

  @Test
  public void shouldBeAbleToGetThePathOfJustTheParent() throws Exception {
    File filePath = updater.getCorrectPathForTheDestination("classes/files/moreFiles/TestFile");
    assertSubString(portablePath("classes/files/moreFiles"), filePath.getPath());
  }

  private String portablePath(String path) {
    return path.replace("/", System.getProperty("file.separator"));
  }

  @Test
  public void shouldCreateTheCorrectPathForGivenPath() throws Exception {
    String filePath = updater.getCorrectPathFromJar("FitNesseRoot/files/moreFiles/TestFile");
    assertEquals("Resources/FitNesseRoot/files/moreFiles/TestFile", filePath);
  }

  @Test
  public void shouldCreateSomeFilesInTheRooTDirectory() throws Exception {
    for (Update update : updater.getUpdates()) {
        update.doUpdate();
    }
    File testFile = new File(context.getRootPagePath(), "files/TestFile");
    File bestFile = new File(context.getRootPagePath(), "files/BestFile");
    File specialFile = new File(context.getRootPagePath(), "SpecialFile");
    assertTrue(testFile.exists());
    assertTrue(bestFile.exists());
    assertTrue(specialFile.exists());
    assertFalse(testFile.isDirectory());
    assertFalse(bestFile.isDirectory());
    assertFalse(specialFile.isDirectory());
  }

  @Test
  public void shouldReplaceFitNesseRootWithDirectoryRoot() throws Exception {
    String filePath = "FitNesseRoot/someFolder/someFile";
    setTheContext("MyNewRoot");
    updater = new UpdaterImplementation(context);
    File updatedPath = updater.getCorrectPathForTheDestination(filePath);
    assertEquals(portablePath("testDir/MyNewRoot/someFolder"), updatedPath.getPath());
  }

  @Test
  public void updatesShouldBeRunIfCurrentVersionNotAlreadyUpdated() throws Exception {
    String version = "TestVersion";
    updater.setFitNesseVersion(version);

    File propertiesFile = new File("testDir/RooT/properties");
    FileUtil.deleteFile(propertiesFile);
    assertFalse(propertiesFile.exists());

    updater.setUpdates(new Update[]{
      new UpdateSpy()
    });
    updater.update();
    assertTrue(updateDone);
    assertTrue(propertiesFile.exists());

    Properties properties = updater.loadProperties();
    assertTrue(properties.containsKey("Version"));
    assertEquals(version, properties.get("Version"));
    FileUtil.deleteFile(propertiesFile);
  }

  @Test
  public void updatesShouldNotBeRunIfCurrentVersionAlreadyUpdated() throws Exception {
    String version = "TestVersion";
    updater.setFitNesseVersion(version);
    Properties properties = updater.getProperties();
    properties.put("Version", version);
    updater.setUpdates(new Update[]{
      new UpdateSpy()
    });
    updater.update();
    assertFalse(updateDone);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionInNoUpdateFileExists() throws Exception {
    FileUtil.deleteFile(updateList);
    updater.tryToParseTheFileIntoTheList(updateList,new ArrayList<String>());
  }

  @After
  public void tearDown() {
    FileUtil.deleteFileSystemDirectory("classes/Resources");
    FileUtil.deleteFileSystemDirectory(testDir);
  }

  private class UpdateSpy implements Update {
    public String getName() {
      return "test";
    }

    public String getMessage() {
      return "test";
    }

    public boolean shouldBeApplied() {
      return true;
    }

    public void doUpdate() {
      updateDone = true;
    }
  }
}
