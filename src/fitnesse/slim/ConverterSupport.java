package fitnesse.slim;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.PropertyEditorConverter;

class ConverterSupport {

  public static <T> Converter<T> getConverter(Class<T> k) {
    Converter<T> c = ConverterRegistry.getConverterForClass(k);
    if (c != null)
      return c;
    PropertyEditor pe = PropertyEditorManager.findEditor(k);
    if (pe != null) {
      return new PropertyEditorConverter<T>(pe);
    }
    return null;
  }

  public static Object[] convertArgs(Object[] args, Class<?>[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      convertedArgs[i] = convertArg(args[i], argumentTypes[i]);
    }
    return convertedArgs;
  }

  @SuppressWarnings("unchecked")
  private static <T> T convertArg(Object arg, Class<T> argumentType) throws SlimError {
    if (arg == null || argumentType.isInstance(arg)) {
      // arg may be a List or an instance that comes from the variable store
      return (T) arg;
    }
    Converter<T> converter = getConverter(argumentType);
    if (converter != null) {
      return converter.fromString(arg.toString());
    }
    throw new SlimError(String.format("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER %s.>>",
        argumentType.getName()));
  }

}
