package fitnesse.slim.converters;

import fitnesse.slim.SlimError;
import fitnesse.util.StringUtils;

import fitnesse.slim.Converter;

public class GenericEnumConverter<T extends Enum<T>> implements Converter<T> {

  private final Class<T> enumClass;

  public GenericEnumConverter(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public String toString(T o) {
    return o != null ? o.name() : NULL_VALUE;
  }

  @Override
  public T fromString(String name) {
    if (StringUtils.isBlank(name))
      return null;

    try {
      return Enum.valueOf(enumClass, name);
    } catch (IllegalArgumentException e) {
      for (T value : enumClass.getEnumConstants()) {
        if (value.name().equalsIgnoreCase(name)) {
          return value;
        }
      }
      throw new SlimError(String.format("message:<<Can't convert %s to enum value of type %s.>>", name, enumClass.getName()), e);
    }
  }
}
