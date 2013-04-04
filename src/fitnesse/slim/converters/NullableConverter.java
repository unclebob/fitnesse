package fitnesse.slim.converters;

import fitnesse.slim.Converter;

/**
 * A converter that decorates another converter, but handles <code>null</code>
 * values.
 * <p>
 * If the input is <code>null</code> or "null" (case-insensitive) the converted
 * value will be <code>null</code>. In all other cases the conversion is handed
 * over to the delegate converter.
 * 
 * @author Philipp Jardas &lt;philipp@jardas.de&gt;
 */
public final class NullableConverter<T> implements Converter<T> {
  private static final String NULL = "null";
  private final Converter<T> delegate;

  private NullableConverter(final Converter<T> delegate) {
    this.delegate = delegate;
  }

  public static <T> Converter<T> decorate(final Converter<T> converter) {
    return new NullableConverter<T>(converter);
  }

  @Override
  public T fromString(final String arg) {
    if (arg == null || NULL.equalsIgnoreCase(arg)) {
      return null;
    }

    return delegate.fromString(arg);
  }

  @Override
  public String toString(final T o) {
    if (o == null) {
      return NULL;
    }

    return delegate.toString(o);
  }
}
