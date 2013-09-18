package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.Today;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;
import util.DateAlteringClock;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TodayExtensionTest {

    @Before
    public void setUp() {
        new DateAlteringClock(new GregorianCalendar(2003, 2, 4, 15, 6, 7).getTime()).freeze();
        SymbolProvider.wikiParsingProvider.add(new MonthsFromToday());
        SymbolProvider.tableParsingProvider.add(new MonthsFromToday());
    }

    @After
    public void tearDown() {
        Clock.restoreDefaultClock();
    }

    @Test
    public void translatesMonthsFromTodays() {
        ParserTestHelper.assertTranslatesTo("!monthsFromToday", "04 Mar, 2003");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -t", "04 Mar, 2003 15:06");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -xml", "2003-03-04T15:06:07");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (MMM)", "Mar");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (dd MMM)", "04 Mar");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (dd MMM", "!monthsFromToday (dd MMM");
    }

    @Test
    public void translatesWithDayIncrements() {
        ParserTestHelper.assertTranslatesTo("!monthsFromToday +5", "04 Aug, 2003");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday +10", "04 Jan, 2004");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -5", "04 Oct, 2002");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -5.", "04 Oct, 2002.");
    }

    @Test
    public void translatesWithDayIncrementsAndCustomFormat() {
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (ddMMM) +5", "04Aug");
    }

    @Test
    public void translatesInTable() {
        ParserTestHelper.assertTranslatesTo("|!monthsFromToday (ddMMM)|\n", ParserTestHelper.tableWithCell("04Mar"));
        ParserTestHelper.assertTranslatesTo("|!monthsFromToday -t +2|\n", ParserTestHelper.tableWithCell("04 May, 2003 15:06"));
    }

    private static class MonthsFromToday extends Today {
        public MonthsFromToday() {
            super("MonthsFromToday", "!monthsFromToday", Calendar.MONTH);
        }
    }
}
