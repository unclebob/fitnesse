package fitnesse.slim.converters;

public class PrimitiveDoubleConverter extends DoubleConverter {
  public static final Double DEFAULT_VALUE = Double.valueOf(0);

  @Override
  public Double fromString(String arg) {
    Double d = super.fromString(arg);
    return d != null ? d : DEFAULT_VALUE;
  }
}
