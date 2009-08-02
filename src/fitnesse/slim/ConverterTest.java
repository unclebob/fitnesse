package fitnesse.slim;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import fitnesse.slim.converters.DateConverter;

public class ConverterTest {
  @Test
  public void convertDate() throws Exception {
    DateConverter converter = new DateConverter();
    Date date = (Date) converter.fromString("5-May-2009");
    assertEquals("05-May-2009", converter.toString(date));
  }
}
