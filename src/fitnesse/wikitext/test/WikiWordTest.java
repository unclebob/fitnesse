package fitnesse.wikitext.test;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.WikiWord;
import fitnesse.wikitext.parser.WikiWordBuilder;
import fitnesse.wikitext.parser.WikiWordPath;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WikiWordTest {
    private TestRoot root;
    private WikiPage pageOne;
    private WikiPage pageOneTwo;
    private WikiPage pageOneTwoThree;
    private WikiPage pageOneThree;
    private WikiPage root2;
    private PageCrawler crawler;

    @Before
    public void setUp() throws Exception {
        root = new TestRoot();
        pageOne = root.makePage("PageOne");
        pageOneTwo = root.makePage(pageOne, "PageOne2");
        pageOneTwoThree = root.makePage(pageOneTwo, "PageThree");
        pageOneThree = root.makePage(pageOne, "PageThree");
        root2 = InMemoryPage.makeRoot("RooT");
        crawler = root2.getPageCrawler();
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
          "PageNine<a title=\"create page\" href=\"PageNine?edit&nonExistent=true\">[?]</a>");
    }

    @Test
    public void regracesWikiWords() throws Exception {
        root.setPageData(pageOne, "!define " + WikiWord.REGRACE_LINK + " {true}\nPageOne\n!define " + WikiWord.REGRACE_LINK + " {false}\n");
        assertTrue(ParserTestHelper.translateTo(pageOne).indexOf(wikiLink("PageOne", "Page One")) >= 0);
    }

    @Test
    public void testIsSingleWikiWord() throws Exception {
      assertTrue(WikiWordPath.isSingleWikiWord("WikiWord"));
      assertFalse(WikiWordPath.isSingleWikiWord("notWikiWord"));
      assertFalse(WikiWordPath.isSingleWikiWord("NotSingle.WikiWord"));
    }

    @Test
    public void testIsWikiWord() throws Exception {
       assertEquals(true, WikiWordPath.isWikiWord("HelloThere"));
       assertEquals(false, WikiWordPath.isWikiWord("not.a.wiki.word"));
    }

    @Test
    public void testBackwardSearchWidget() throws Exception {
      //todo: use TestRoot
      WikiPage top = addPage(root2, "TopPage");
      WikiPage target = addPage(top, "TargetPage");
      WikiPage referer = addPage(target, "ReferingPage");
      @SuppressWarnings("unused")
      WikiPage subTarget = addPage(target, "SubTarget");

      String actual = WikiWordBuilder.expandPrefix(referer, "<TargetPage.SubTarget");
      assertEquals(".TopPage.TargetPage.SubTarget", actual);

      actual = WikiWordBuilder.expandPrefix(referer, "<NoSuchPage");
      assertEquals(".NoSuchPage", actual);

      PageData data = referer.getData();
      data.setContent("<TargetPage.SubTarget");
      referer.commit(data);
      String renderedLink = referer.getData().getHtml();
      assertEquals("<a href=\"TopPage.TargetPage.SubTarget\">&lt;TargetPage.SubTarget</a>", renderedLink);
    }

    private WikiPage addPage(WikiPage parent, String childName) throws Exception {
        return crawler.addPage(parent, PathParser.parse(childName));
    }

    private String wikiLink(String link, String text) {
        return "<a href=\"" + link + "\">" + text + "</a>";
    }
}
