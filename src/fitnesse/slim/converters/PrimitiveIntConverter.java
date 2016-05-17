package fitnesse.slim.converters;

public class PrimitiveIntConverter extends IntConverter {
  public static final Integer DEFAULT_VALUE = Integer.valueOf(0);

  @Override
  public Integer fromString(String arg) {
    Integer i = super.fromString(arg);
    return i != null ? i : DEFAULT_VALUE;
  }
}
