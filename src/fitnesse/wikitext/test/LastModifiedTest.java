package fitnesse.wikitext.test;

import fitnesse.wiki.PageData;
import org.junit.Test;
import util.SystemTimeKeeper;
import util.TestTimeKeeper;

import java.util.GregorianCalendar;

public class LastModifiedTest {
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
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 5, 6, 7).getTime());
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
                .withProperty(PageData.PropertyLAST_MODIFIED, date);
    }

    private TestSourcePage makeTestPage() {
        return new TestSourcePage().withContent("!lastmodified");
    }

}
