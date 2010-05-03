package fitnesse.wikitext.test;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.WikiSourcePage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WikiSourcePageTest {

    @Test
    public void getsChildren() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne");
        root.makePage(page, "PageTwo");
        root.makePage(page, "PageThree");
        WikiSourcePage source = new WikiSourcePage(page);
        ArrayList<String> names = new ArrayList<String>();
        for (SourcePage child: source.getChildren()) names.add(child.getName());

        assertEquals(2, names.size());
        assertTrue(names.contains("PageTwo"));
        assertTrue(names.contains("PageThree"));
    }

    @Test
    public void getsVirtualChildren() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne");
        root.makePage(page, "PageTwo");
        root.makePage(page, "PageThree");
        WikiPage virtualPage = root.makePage("VirtualPage");
        VirtualCouplingExtension extension = (VirtualCouplingExtension) virtualPage.getExtension(VirtualCouplingExtension.NAME);
        extension.setVirtualCoupling(new MockVirtualCouplingPage(page));

        WikiSourcePage source = new WikiSourcePage(virtualPage);
        ArrayList<String> names = new ArrayList<String>();
        for (SourcePage child: source.getChildren()) names.add(child.getName());

        assertEquals(2, names.size());
        assertTrue(names.contains("PageTwo"));
        assertTrue(names.contains("PageThree"));
    }

    private class MockVirtualCouplingPage extends VirtualCouplingPage {
        private WikiPage mockVirtualPage;
        public MockVirtualCouplingPage(WikiPage mockVirtualPage) {
            super(mockVirtualPage);
            this.mockVirtualPage = mockVirtualPage;
        }

        public List<WikiPage> getChildren() throws Exception {
          return mockVirtualPage.getChildren();
        }
    }

    @Test
    public void getsUrlForPage() throws Exception {
        WikiPage test = new TestRoot().makePage("MyPage");
        assertEquals("WikiPath", new WikiSourcePage(test).makeUrl("WikiPath"));
    }

    @Test
    public void getsUrlForProxyPage() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT");
        ProxyPage virtualPage = new ProxyPage("VirtualPage", root, "host", 9999, PathParser.parse("RealPage.VirtualPage"));
        assertEquals("http://host:9999/RealPage.WikiPath", new WikiSourcePage(virtualPage).makeUrl("WikiPath"));
    }
}
