package fitnesse.slim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.DateConverter;

public class ConverterTest {
  @Test
  public void convertDate() throws Exception {
    assertConverts(new DateConverter(), "05-May-2009");
  }
  
  @Test
  public void convertDateWithoutLeadingZero() throws Exception {
    assertConverts("05-May-2009", new DateConverter(), "5-May-2009");
  }

  @Test
  public void convertBooleanTrue() throws Exception {
    BooleanConverter converter = new BooleanConverter();
    assertConverts(converter, "true");
    assertConverts("true", converter, "True");
    assertConverts("true", converter, "TRUE");
    assertConverts("true", converter, "YES");
    assertConverts("true", converter, "yes");
  }

  @Test
  public void convertBooleanFalse() throws Exception {
    BooleanConverter converter = new BooleanConverter();
    assertConverts(converter, "false");
    assertConverts("false", converter, "FALSE");
    assertConverts("false", converter, "False");
    assertConverts("false", converter, "no");
    assertConverts("false", converter, "NO");
    assertConverts("false", converter, "0");
    assertConverts("false", converter, "1");
    assertConverts("false", converter, "x");
  }
  
  private void assertConverts(Converter converter, String value) {
    assertConverts(value, converter, value);
  }
  
  private void assertConverts(String expected, Converter converter, String value) {
    assertEquals(expected, converter.toString(converter.fromString(value)));
  }
}
