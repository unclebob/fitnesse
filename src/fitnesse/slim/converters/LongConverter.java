package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class LongConverter implements Converter<Long> {
  public String toString(Long o) {
    return o.toString();
  }

  public Long fromString(String arg) {
    return Long.parseLong(arg);
  }
}
