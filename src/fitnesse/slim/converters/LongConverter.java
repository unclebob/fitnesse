package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;
import fitnesse.util.StringUtils;

public class LongConverter implements Converter<Long> {
  public String toString(Long o) {
    return o != null ? o.toString() : NULL_VALUE;
  }

  public Long fromString(String arg) {
    try {
      return !StringUtils.isBlank(arg) ? Long.valueOf(arg) : null;
    } catch (NumberFormatException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to long.>>", arg), e);
    }
  }
}
