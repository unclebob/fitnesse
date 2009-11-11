package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;

public class ExternalTestPageTest {

    @Test
    public void PageDataIsFileContents() throws Exception {
        FileSystem fileSystem = new MemoryFileSystem();
        fileSystem.makeFile("somewhere/myfile.html", "stuff");
        assertEquals("stuff", new ExternalTestPage("somewhere/myfile.html", "myfile.html", null, fileSystem).getData().getContent());
    }
}
