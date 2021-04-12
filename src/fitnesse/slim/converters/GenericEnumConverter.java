package fitnesse.slim.converters;

import fitnesse.slim.SlimError;

public class GenericEnumConverter<T extends Enum<T>> extends ConverterBase<T> {

  private final Class<T> enumClass;

  public GenericEnumConverter(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public String getString(T o) {
    return o.name();
  }

  @Override
  public T getObject(String name) {
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
