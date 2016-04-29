import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.Assert.*

class WikiFileListBuilderTest {

  File updateList
  File updateDoNotCopyOverList

  @Before
  public void setUp() throws IOException {
    updateList = File.createTempFile("fitnesse", "")
    updateDoNotCopyOverList = File.createTempFile("fitnesse", "")
    createMultiLevelDirectory();
  }

  @Test
  public void shouldKnowIfAGivenDirectoryExists() throws Exception {
    File testFolder = new File("TestFolder");
    testFolder.mkdir();
    WikiFileListBuilder updater = new WikiFileListBuilder(["TestFolder"]);
    assertTrue(updater.directoriesAreValid());
    testFolder.deleteDir();
    assertFalse(updater.directoriesAreValid());
  }

  @Test
  public void shouldMakeUpdateListWithMultiLevelFolders() throws Exception {
    String content = runCreateFileAndGetContent(["MasterFolder"]);
    assertSubString("MasterFolder/content.txt\n", content);
    assertSubString("MasterFolder/TestFolder/content.txt\n", content);
  }

  @Test
  public void shouldNotAddBackupAndMetadataFiles() throws Exception {
    String content = runCreateFileAndGetContent(["MasterFolder"]);
    assertDoesntHaveRegexp("MasterFolder/TestFolder/backup.zip", content);
    assertDoesntHaveRegexp("MasterFolder/TestFolder/.DS_Store", content);
  }

  @Test
  public void shouldKnowWhichSpecialFilesNotToInclude() throws Exception {
    String arg1 = "MasterFolder/TestFolder/content.txt";
    String arg2 = "MasterFolder/TestFolder/properties.xml";
    String content = runCreateFileAndGetContent(["MasterFolder/TestFolder"], [arg1, arg2]);
    assertDoesntHaveRegexp("TestFolder/content.txt", content);
    assertDoesntHaveRegexp("TestFolder/properties.xml", content);
  }

  @Test
  public void shouldPutSpecialFilesInDifferentList() throws Exception {
    String arg1 = "MasterFolder/TestFolder/content.txt";
    String arg2 = "MasterFolder/TestFolder/properties.xml";
    def updater = new WikiFileListBuilder(["MasterFolder/TestFolder"], [arg1, arg2], updateList, updateDoNotCopyOverList)
    File doNotUpdateFile = updater.createDoNotUpdateList();
    String doNotUpdateContent = doNotUpdateFile.text
    doNotUpdateFile.delete()
    assertSubString("TestFolder/content.txt", doNotUpdateContent);
    assertSubString("TestFolder/properties.xml", doNotUpdateContent);
  }

  class MockedWikiFileListBuilder extends WikiFileListBuilder {
    def exitCalled = false

    MockedWikiFileListBuilder() {
      super([])
    }

    @Override
    public boolean directoriesAreValid() { false }
    void exit() { exitCalled = true }
  }

  @Test(expected = RuntimeException)
  public void testMainUnhappyPath() throws Exception {
    WikiFileListBuilder updaterMock = new MockedWikiFileListBuilder();
    updaterMock.createUpdateLists();
  }

  private String runCreateFileAndGetContent(List<String> mainDirs = [], List<String> doNotCopyDirs = []) throws Exception {
    WikiFileListBuilder updater = new WikiFileListBuilder(mainDirs, doNotCopyDirs, updateList, updateDoNotCopyOverList);
    File resultFile = updater.createUpdateList();
    String content = resultFile.text
    resultFile.delete()
    return content;
  }

  private static void createMultiLevelDirectory() throws IOException {
    new File("MasterFolder/TestFolder").mkdirs()
    new File("MasterFolder/content.txt").text = ""
    new File("MasterFolder/TestFolder/content.txt").text = ""
    new File("MasterFolder/TestFolder/properties.xml").text = ""
    new File("MasterFolder/TestFolder/backup.zip").text = ""
    new File("MasterFolder/TestFolder/.DS_Store").text = ""
  }

  @After
  public void tearDown() {
    new File("MasterFolder").deleteDir();
  }

  public static void assertSubString(String substring, String string) {
    if (!string.contains(substring))
      fail("substring '" + substring + "' not found in string '" + string + "'.");
  }

  public static void assertHasRegexp(String regexp, String output) {
    Matcher match = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
    boolean found = match.find();
    if (!found)
      fail("The regexp <" + regexp + "> was not found in: " + output + ".");
  }

  public static void assertDoesntHaveRegexp(String regexp, String output) {
    Matcher match = Pattern.compile(regexp, Pattern.MULTILINE).matcher(output);
    boolean found = match.find();
    if (found)
      fail("The regexp <" + regexp + "> was found.");
  }

}
