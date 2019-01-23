package fitnesse.slim.converters;

public class PrimitiveLongConverter extends LongConverter {
  public static final Long DEFAULT_VALUE = Long.valueOf(0);

  @Override
  public Long fromString(String arg) {
    Long i = super.fromString(arg);
    return i != null ? i : DEFAULT_VALUE;
  }
}
