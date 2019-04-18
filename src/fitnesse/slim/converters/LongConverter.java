package fitnesse.slim.converters;

import fitnesse.slim.SlimError;

public class LongConverter extends ConverterBase<Long> {

  @Override
  protected Long getObject(String arg) {
    try {
      return Long.valueOf(arg);
    } catch (NumberFormatException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to long.>>", arg), e);
    }
  }
}
