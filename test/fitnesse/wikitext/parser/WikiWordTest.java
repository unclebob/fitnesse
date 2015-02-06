package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WikiWordTest {
    private TestRoot root;
    private WikiPage pageOne;
    private WikiPage pageOneTwo;
    private WikiPage pageOneTwoThree;
    private WikiPage pageOneThree;
    private WikiPage root2;

    @Before
    public void setUp() throws Exception {
        root = new TestRoot();
        pageOne = root.makePage("PageOne");
        pageOneTwo = root.makePage(pageOne, "PageOne2");
        pageOneTwoThree = root.makePage(pageOneTwo, "PageThree");
        pageOneThree = root.makePage(pageOne, "PageThree");
        root2 = InMemoryPage.makeRoot("RooT");
    }

    @Test
    public void translatesWikiWords() throws Exception {
        ParserTestHelper.assertTranslatesTo(pageOne, "PageOne", wikiLink("PageOne", "PageOne"));
        ParserTestHelper.assertTranslatesTo(pageOneTwo, "PageOne2", wikiLink("PageOne.PageOne2", "PageOne2"));
        ParserTestHelper.assertTranslatesTo(pageOneThree, ".PageOne", wikiLink("PageOne", ".PageOne"));
        ParserTestHelper.assertTranslatesTo(pageOne, ">PageOne2", wikiLink("PageOne.PageOne2", "&gt;PageOne2"));
        ParserTestHelper.assertTranslatesTo(pageOneTwoThree, "<PageOne", wikiLink("PageOne", "&lt;PageOne"));
    }
    
    @Test
    public void translatesMissingWikiWords() throws Exception {
        ParserTestHelper.assertTranslatesTo(pageOne, "PageNine",
          "PageNine<a title=\"create page\" href=\"PageNine?edit&amp;nonExistent=true\">[?]</a>");
    }

    @Test
    public void regracesWikiWords() throws Exception {
        root.setPageData(pageOne, "!define " + WikiWord.REGRACE_LINK + " {true}\nPageOne\n!define " + WikiWord.REGRACE_LINK + " {false}\n");
        assertTrue(ParserTestHelper.translateTo(pageOne).contains(wikiLink("PageOne", "Page One")));
    }

    @Test
    public void testBackwardSearchWidget() throws Exception {
      //todo: use TestRoot
      WikiPage top = addPage(root2, "TopPage");
      WikiPage target = addPage(top, "TargetPage");
      WikiPage referer = addPage(target, "ReferingPage");
      addPage(target, "SubTarget");

      String actual = WikiWordBuilder.expandPrefix(referer, "<TargetPage.SubTarget");
      assertEquals(".TopPage.TargetPage.SubTarget", actual);

      actual = WikiWordBuilder.expandPrefix(referer, "<NoSuchPage");
      assertEquals(".NoSuchPage", actual);

      PageData data = referer.getData();
      data.setContent("<TargetPage.SubTarget");
      referer.commit(data);
      String renderedLink = referer.getHtml();
      assertEquals("<a href=\"TopPage.TargetPage.SubTarget\">&lt;TargetPage.SubTarget</a>", renderedLink);
    }

    private WikiPage addPage(WikiPage parent, String childName) throws Exception {
        return WikiPageUtil.addPage(parent, PathParser.parse(childName), "");
    }

    private String wikiLink(String link, String text) {
        return "<a href=\"" + link + "\">" + text + "</a>";
    }
}
