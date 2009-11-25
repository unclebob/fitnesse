package fitnesse.wiki;

import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExternalTestPageTest {

    @Test
    public void PageDataIsFileContents() throws Exception {
        assertEquals("!-stuff-!", makePage("somewhere", "myfile.html", "stuff").getData().getContent());
    }

    @Test
    public void WhenHasNoTableThenIsNotTest() throws Exception {
        assertFalse(makePage("somewhere", "myfile.html", "stuff").getData().hasAttribute(PageType.TEST.toString()));
    }

    @Test
    public void WhenHasTableThenIsTest() throws Exception {
        assertTrue(makePage("somewhere", "myfile.html", "stuff and <table>").getData().hasAttribute(PageType.TEST.toString()));
    }

    private ExternalTestPage makePage(String directory, String name, String content) throws Exception {
        FileSystem fileSystem = new MemoryFileSystem();
        String path = directory + "/" + name;
        fileSystem.makeFile(path, content);
        return new ExternalTestPage(path, name, null, fileSystem);
    }
}
