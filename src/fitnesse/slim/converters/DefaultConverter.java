package fitnesse.slim.converters;

public class DefaultConverter extends ConverterBase<Object> {
  @Override
  public Object fromString(String arg) {
    return arg;
  }

  @Override
  protected Object getObject(String arg) {
    // we don't expect this to be called
    return arg;
  }
}
