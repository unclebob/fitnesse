package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.util.StringUtils;

public abstract class ConverterBase<T> implements Converter<T> {
  @Override
  public String toString(T o) {
    return o == null ? NULL_VALUE : getString(o);
  }

  protected String getString(T o) {
    return o.toString();
  }

  @Override
  public T fromString(String arg) {
    return StringUtils.isBlank(arg) ? null : getObject(arg);
  }

  protected abstract T getObject(String arg);
}
