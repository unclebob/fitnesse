package fitnesse.wiki;

import util.FileSystem;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import util.MemoryFileSystem;

public class ExternalSuitePageTest {
    @Test
    public void ContentIsTableOfContents() throws Exception {
        assertEquals("!contents", new ExternalSuitePage("somewhere", "MyTest", null, null).getData().getContent());
    }

    @Test
    public void ChildrenAreLoaded() throws Exception {
        FileSystem fileSystem = new MemoryFileSystem();
        fileSystem.makeFile("somewhere/MyTest/myfile.html", "stuff");
        assertEquals(1, new ExternalSuitePage("somewhere/MyTest", "MyTest", null, fileSystem).getChildren().size());
    }
}
