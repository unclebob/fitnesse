package fitnesse.wikitext.parser;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TodayExtensionTest {

  private Locale originalLocale;

    @Before
    public void setUp() {
      originalLocale = Locale.getDefault();
      Locale.setDefault(Locale.US);
      new DateAlteringClock(new GregorianCalendar(2003, 2, 4, 15, 6, 7).getTime()).freeze();
      SymbolProvider.wikiParsingProvider.add(new MonthsFromToday());
      SymbolProvider.tableParsingProvider.add(new MonthsFromToday());
    }

    @After
    public void tearDown() {
      Clock.restoreDefaultClock();
      Locale.setDefault(originalLocale);
    }

    @Test
    public void translatesMonthsFromTodays() {
        ParserTestHelper.assertTranslatesTo("!monthsFromToday", "04 Mar, 2003");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -t", "04 Mar, 2003 15:06");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -xml", "2003-03-04T15:06:07");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (MMM)", "Mar");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (dd MMM)", "04 Mar");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday (dd MMM" /* eof */, "04 Mar");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -t.", "04 Mar, 2003 15:06.");
        ParserTestHelper.assertTranslatesTo("!monthsFromToday -xml.", "2003-03-04T15:06:07.");
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
        ParserTestHelper.assertTranslatesTo("|!monthsFromToday -t +2.|\n", ParserTestHelper.tableWithCell("04 May, 2003 15:06."));
    }

    private static class MonthsFromToday extends Today {
        public MonthsFromToday() {
            super("MonthsFromToday", "!monthsFromToday", Calendar.MONTH);
        }
    }
}
