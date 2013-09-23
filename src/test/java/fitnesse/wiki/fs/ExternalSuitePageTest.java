package fitnesse.wiki.fs;

import fitnesse.wiki.mem.MemoryFileSystem;
import org.junit.Assert;
import org.junit.Test;

public class ExternalSuitePageTest {
    @Test
    public void ContentIsTableOfContents() throws Exception {
        Assert.assertEquals("!contents", new ExternalSuitePage("somewhere", "MyTest", null, null).getData().getContent());
    }

    @Test
    public void ChildrenAreLoaded() throws Exception {
        FileSystem fileSystem = new MemoryFileSystem();
        fileSystem.makeFile("somewhere/MyTest/myfile.html", "stuff");
        Assert.assertEquals(1, new ExternalSuitePage("somewhere/MyTest", "MyTest", null, fileSystem).getChildren().size());
    }
}
