package fitnesse.slim.converters;

public class PrimitiveLongConverter extends LongConverter {
  public static final Long DEFAULT_VALUE = 0L;

  @Override
  public Long fromString(String arg) {
    Long l = super.fromString(arg);
    return l != null ? l : DEFAULT_VALUE;
  }
}
