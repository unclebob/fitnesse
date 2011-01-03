package fitnesse.wikitext.test;

import org.junit.Test;
import util.SystemTimeKeeper;
import util.TestTimeKeeper;

import java.util.GregorianCalendar;

public class TodayTest {
    @Test
    public void translatesTodays() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTest.assertTranslatesTo("!today", "04 Mar, 2002");
        ParserTest.assertTranslatesTo("!today -t", "04 Mar, 2002 15:06");
        ParserTest.assertTranslatesTo("!today -xml", "2002-03-04T15:06:07");
        ParserTest.assertTranslatesTo("!today (MMM)", "Mar");
        ParserTest.assertTranslatesTo("!today (dd MMM)", "04 Mar");
        ParserTest.assertTranslatesTo("!today (dd MMM", "!today (dd MMM");
    }

    @Test
    public void translatesWithDayIncrements() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTest.assertTranslatesTo("!today +5", "09 Mar, 2002");
        ParserTest.assertTranslatesTo("!today -5", "27 Feb, 2002");
    }

    @Test
    public void translatesWithDayIncrementsAndCustomFormat() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTest.assertTranslatesTo("!today (ddMMM) +5", "09Mar");
    }
}
