package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateConverter implements Converter {
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

  public String toString(Object o) {
    return dateFormat.format((Date)o);
  }

  public Object fromString(String arg) {
    try {
      return dateFormat.parse(arg);
    } catch (ParseException e) {
      throw new SlimError("Can't parse date " + arg, e);
    }
  }
}
