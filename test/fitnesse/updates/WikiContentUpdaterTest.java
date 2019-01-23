package fitnesse.updates;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import util.GradleSupport;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertSubString;

public class WikiContentUpdaterTest {
  private File updateList;
  private File updateDoNotCopyOver;
  public static final String testDir = "testDir";
  public static final String rootName = "RooT";

  protected WikiPage root;
  protected WikiContentUpdater updater;
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
    updater = new WikiContentUpdater(context);
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

  private void createFakeUpdateListFiles() throws IOException {
    updateList = new File(GradleSupport.TEST_CLASSES_DIR + "/Resources/updateList");
    updateDoNotCopyOver = new File(GradleSupport.TEST_CLASSES_DIR +"/Resources/updateDoNotCopyOverList");
    FileUtil.createFile(updateList, "FitNesseRoot/files/TestFile\nFitNesseRoot/files/BestFile\n");
    FileUtil.createFile(updateDoNotCopyOver, "FitNesseRoot/SpecialFile");
  }

  private void createFakeJarFileResources() throws IOException {
    FileUtil.createFile(GradleSupport.TEST_CLASSES_DIR + "/Resources/FitNesseRoot/files/TestFile","") ;
    FileUtil.createFile(GradleSupport.TEST_CLASSES_DIR + "/Resources/FitNesseRoot/files/BestFile","") ;
    FileUtil.createFile(GradleSupport.TEST_CLASSES_DIR + "/Resources/FitNesseRoot/SpecialFile","");
  }

  @Test
  public void shouldBeAbleToGetUpdateFilesAndMakeAlistFromThem() throws Exception {
    String[] updateArrayList = updater.parseResource("Resources/updateList");
    assertEquals("FitNesseRoot/files/TestFile", updateArrayList[0]);
    assertEquals("FitNesseRoot/files/BestFile", updateArrayList[1]);

    updateArrayList = updater.parseResource("Resources/updateDoNotCopyOverList");
    assertEquals("FitNesseRoot/SpecialFile", updateArrayList[0]);
  }

  @Test
  public void shouldBeAbleToGetThePathOfJustTheParent() throws Exception {
    File filePath = updater.getCorrectPathForTheDestination(GradleSupport.TEST_CLASSES_DIR + "/files/moreFiles/TestFile");
    assertSubString(portablePath(GradleSupport.TEST_CLASSES_DIR + "/files/moreFiles"), filePath.getPath());
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

    updater.update();

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
    updater = new WikiContentUpdater(context);
    File updatedPath = updater.getCorrectPathForTheDestination(filePath);
    assertEquals(portablePath("testDir/MyNewRoot/someFolder"), updatedPath.getPath());
  }

  @Test
  public void testProperties() throws Exception {
    updater = new WikiContentUpdater(context) {
      @Override
      List<Update> makeAllUpdates() {
        return Collections.emptyList();
      }
    };
    File file = new File(new File(testDir, rootName), "properties");
    assertFalse(file.exists());
    updater.update();
    assertTrue(file.exists());
  }

  @Test
  public void updatesShouldBeRunIfCurrentVersionNotAlreadyUpdated() throws Exception {
    updater = new WikiContentUpdater(context) {
      @Override
      List<Update> makeAllUpdates() {
        return Collections.<Update>singletonList(new UpdateSpy());
      }
    };
    String version = "TestVersion";
    updater.setFitNesseVersion(version);

    File propertiesFile = new File("testDir/RooT/properties");
    FileUtil.deleteFile(propertiesFile);
    assertFalse(propertiesFile.exists());


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
    updater = new WikiContentUpdater(context) {
      @Override
      List<Update> makeAllUpdates() {
        return Collections.<Update>singletonList(new UpdateSpy());
      }
    };
    String version = "TestVersion";
    updater.setFitNesseVersion(version);
    Properties properties = updater.getProperties();
    properties.put("Version", version);
    updater.update();
    assertFalse(updateDone);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionInNoUpdateFileExists() throws Exception {
    FileUtil.deleteFile(updateList);
    updater.parseResource(GradleSupport.TEST_CLASSES_DIR + "/Resources/updateList");
  }

  @After
  public void tearDown() throws IOException {
    FileUtil.deleteFileSystemDirectory(GradleSupport.TEST_CLASSES_DIR + "/Resources");
    FileUtil.deleteFileSystemDirectory(testDir);
  }

  private class UpdateSpy implements Update {
    @Override
    public String getName() {
      return "test";
    }

    @Override
    public String getMessage() {
      return "test";
    }

    @Override
    public boolean shouldBeApplied() {
      return true;
    }

    @Override
    public void doUpdate() {
      updateDone = true;
    }
  }
}
