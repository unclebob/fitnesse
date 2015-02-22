package fitnesse.slim.converters;

public class PrimitiveBooleanConverter extends BooleanConverter {
  public static final Boolean DEFAULT_VALUE = Boolean.FALSE;

  @Override
  public Boolean fromString(String arg) {
    Boolean b = super.fromString(arg);
    return b != null ? b : DEFAULT_VALUE;
  }
}
