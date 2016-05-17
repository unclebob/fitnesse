package fitnesse.slim.converters;

public class PrimitiveCharConverter extends CharConverter {
  public static final Character DEFAULT_VALUE = Character.MIN_VALUE;

  @Override
  public Character fromString(String arg) {
    Character c = super.fromString(arg);
    return c != null ? c : DEFAULT_VALUE;
  }
}
