package fitnesse.wikitext.parser;

import java.util.GregorianCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperty;

public class LastModifiedTest {

    @Before
    public void setUp() {
        new DateAlteringClock(new GregorianCalendar(2002, 2, 4, 5, 6, 7).getTime()).freeze();
    }

    @After
    public void tearDown() {
        Clock.restoreDefaultClock();
    }

    @Test
    public void scansLastModified() {
        ParserTestHelper.assertScansTokenType("!lastmodified", "LastModified", true);
    }

    @Test
    public void translatesLastModified() throws Exception {
        TestSourcePage page = makeTestPageWithDate("20010203040506");
        ParserTestHelper.assertTranslatesTo(page, ParserTestHelper.metaHtml("Last modified anonymously on Feb 03, 2001 at 04:05:06 AM"));
    }

    @Test
    public void translatesWithUser() throws Exception {
        TestSourcePage page = makeTestPageWithDate("20010203040506")
                .withProperty(PageData.LAST_MODIFYING_USER, "bob");
        ParserTestHelper.assertTranslatesTo(page, ParserTestHelper.metaHtml("Last modified by bob on Feb 03, 2001 at 04:05:06 AM"));
    }

    @Test
    public void usesNowIfNoDate() throws Exception {
        TestSourcePage page = makeTestPage();
        ParserTestHelper.assertTranslatesTo(page, ParserTestHelper.metaHtml("Last modified anonymously on Mar 04, 2002 at 05:06:07 AM"));
    }

    @Test
    public void usesDateStringIfInvalid() throws Exception {
        TestSourcePage page = makeTestPageWithDate("garbage");
        ParserTestHelper.assertTranslatesTo(page, ParserTestHelper.metaHtml("Last modified anonymously on garbage"));
    }

    private TestSourcePage makeTestPageWithDate(String date) {
        return makeTestPage()
                .withProperty(WikiPageProperty.LAST_MODIFIED, date);
    }

    private TestSourcePage makeTestPage() {
        return new TestSourcePage().withContent("!lastmodified");
    }

}
