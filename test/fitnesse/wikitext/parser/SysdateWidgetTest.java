package fitnesse.wikitext.parser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;

public class SysdateWidgetTest
{

  @Before
  public void setUp()
  {
    new DateAlteringClock(new GregorianCalendar(2002, 2, 18, 15, 6, 7).getTime()).freeze();
  }

  @After
  public void tearDown()
  {
    Clock.restoreDefaultClock();
  }

  @Test
  public void translatesSysdates()
  {
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss})"), is("18.03.2002 15:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy yy:HH:mm:ss})"), is("18.03.2002 02:15:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({yyyy-dd-MM'T'HH:mm:ss})"), is("2002-18-03T15:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({yyyyMMddHHmmss})"), is("20020318150607"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({01.MM.yyyy 00:00:00})"), is("01.03.2002 00:00:00"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({01.MM.yyyy 12:13:14})"), is("01.03.2002 12:13:14"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} +1h)"), is("18.03.2002 16:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} +1y)"), is("18.03.2003 15:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} +1m)"), is("18.03.2002 15:07:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} +1s)"), is("18.03.2002 15:06:08"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} -4M +3h)"), is("18.11.2001 18:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} unix)"), is("1016460367"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy HH:mm:ss} utc)"), is("18.03.2002 14:06:07"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy 23:59:59} last_of_month)"), is("31.03.2002 23:59:59"));
    assertThat(translateToAndExtractValue("!sysdate zeit ({dd.MM.yyyy 12:13:14} last_of_month)"), is("31.03.2002 12:13:14"));
  }

  private static String translateToAndExtractValue(final String input)
  {
    String value = extractValue(translateTo(input));
    System.out.println(value);
    return value;
  }

  private static String translateTo(final String input)
  {
    final TestSourcePage page = new TestSourcePage();
    return ParserTestHelper.translateTo(page, input);
  }

  private static String extractValue(String str)
  {
    String _res = str.replace("<span class=\"meta\">sysdate defined: zeit=", "");
    _res = _res.replace("</span>", "");
    return _res.trim();
  }
}
