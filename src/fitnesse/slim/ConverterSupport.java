package fitnesse.slim;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import fitnesse.slim.converters.ConverterRegistry;

public class ConverterSupport {

  public static Object[] convertArgs(Object[] args, Type[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      if (argumentTypes[i] instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) argumentTypes[i];
        convertedArgs[i] = convertArg(args[i], (Class<?>) parameterizedType.getRawType(), (ParameterizedType) argumentTypes[i]);
      } else
        convertedArgs[i] = convertArg(args[i], (Class<?>) argumentTypes[i], null);
    }
    return convertedArgs;
  }

  @SuppressWarnings("unchecked")
  private static <T> T convertArg(Object arg, Class<T> argumentType, ParameterizedType argumentParameterizedType) throws SlimError {

    if (arg == null || (argumentType.isInstance(arg) && String.class != argumentType)) {
      // arg may be an instance that comes from the variable store
      // But String arguments should always pass through the registered String Converter
      return (T) arg;
    }

    Converter<T> converter = ConverterRegistry.getConverterForClass(argumentType, argumentParameterizedType);
    if (converter != null) {
      return converter.fromString(arg.toString());
    }

    throw new SlimError(String.format("message:<<%s %s.>>", SlimServer.NO_CONVERTER_FOR_ARGUMENT_NUMBER, argumentType.getName()));
  }

}
