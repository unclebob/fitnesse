import groovy.mock.interceptor.MockFor
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.Assert.*

@Ignore
class WikiFileListBuilderTest {
  private WikiFileListBuilder updater;

  @Before
  public void setUp() throws IOException {
    updater = new WikiFileListBuilder();
    createMultiLevelDirectory();
  }

  @Test
  public void canParseTheCommandLine() throws Exception {
    updater.parseCommandLine(["testDir"] as String[]);
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
    updater.parseCommandLine(["TestFolder"] as String[]);
    assertTrue(updater.directoriesAreValid());
    testFolder.deleteDir();
    assertFalse(updater.directoriesAreValid());
  }

  @Test
  public void shouldMakeUpdateListWithMultiLevelFolders() throws Exception {
    String content = runCreateFileAndGetContent(["MasterFolder"] as String[]);
    assertSubString("MasterFolder/content.txt\n", content);
    assertSubString("MasterFolder/TestFolder/content.txt\n", content);
  }

  @Test
  public void shouldNotAddBackupAndMetadataFiles() throws Exception {
    String content = runCreateFileAndGetContent(["MasterFolder"] as String[]);
    assertDoesntHaveRegexp("MasterFolder/TestFolder/backup.zip", content);
    assertDoesntHaveRegexp("MasterFolder/TestFolder/.DS_Store", content);
  }

  @Test
  public void shouldKnowWhichSpecialFilesNotToInclude() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/content.txt";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/properties.xml";
    String content = runCreateFileAndGetContent([arg1, arg2, "MasterFolder/TestFolder"] as String[]);
    assertDoesntHaveRegexp("TestFolder/content.txt", content);
    assertDoesntHaveRegexp("TestFolder/properties.xml", content);
  }

  @Test
  public void shouldPutSpecialFilesInDifferentList() throws Exception {
    String arg1 = "-doNotReplace:MasterFolder/TestFolder/content.txt";
    String arg2 = "-doNotReplace:MasterFolder/TestFolder/properties.xml";
    updater.parseCommandLine([arg1, arg2, "MasterFolder/TestFolder"] as String[]);
    File doNotUpdateFile = updater.createDoNotUpdateList();
    String doNotUpdateContent = doNotUpdateFile.text
    doNotUpdateFile.delete()
    assertSubString("TestFolder/content.txt", doNotUpdateContent);
    assertSubString("TestFolder/properties.xml", doNotUpdateContent);
  }

  @Test
  public void shouldPrunePrefixes() throws Exception {
    String content = runCreateFileAndGetContent(["-baseDirectory:MasterFolder/TestFolder", ""] as String[]);
    assertSubString("content.txt\n", content);
    assertDoesntHaveRegexp("TestFolder/content.txt", content);
  }

  class MockedWikiFileListBuilder extends WikiFileListBuilder {
    def exitCalled = false
    @Override
    public boolean directoriesAreValid() { false }
    void exit() { exitCalled = true }
  }

  @Test
  public void testMainUnhappyPath() throws Exception {
    String[] args = ["foo", "bar"];
    WikiFileListBuilder updaterMock = new MockedWikiFileListBuilder();
    updaterMock.main2(args as String[]);
    assertTrue(updaterMock.exitCalled);
  }

  @Test
  public void shouldSplitUpWindowsLikePathNames() throws Exception {
    String[] args = ["-baseDirectory:C:\\FitNesse/Resources", "MasterFolder"];
    updater.parseCommandLine(args);
    assertEquals(["C:\\FitNesse/Resources/MasterFolder"], updater.getDirectories());

  }

  private String runCreateFileAndGetContent(String[] args) throws Exception {
    updater.parseCommandLine(args);
    File resultFile = updater.createUpdateList();
    String content = resultFile.text
    resultFile.delete()
    return content;
  }

  private void createMultiLevelDirectory() throws IOException {
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
