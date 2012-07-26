package fitnesse.slim;

import org.apache.commons.lang.StringUtils;

public class EnumConverter<T extends Enum<T>> implements Converter<T> {

  private final Class<T> enumClass;

  public EnumConverter(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public String toString(T o) {
    return o.name();
  }

  @Override
  public T fromString(String name) {
    if (StringUtils.isBlank(name))
      return null;
    else
      return Enum.valueOf(enumClass, name);
  }

}
