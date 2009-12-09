package fitnesse.slim;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.List;

import fitnesse.slim.converters.PropertyEditorConverter;

class ConverterSupport {

  public static Converter getConverter(Class<?> k) {
    Converter c = Slim.converters.get(k);
    if (c != null)
      return c;
    PropertyEditor pe = PropertyEditorManager.findEditor(k);
    if (pe != null) {
      return new PropertyEditorConverter(pe);
    }
    return null;
  }

  // todo refactor this mess
  @SuppressWarnings("unchecked")
  public static Object[] convertArgs(Object[] args, Class<?>[] argumentTypes) {
    Object[] convertedArgs = new Object[args.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      Class<?> argumentType = argumentTypes[i];
      if (argumentType == List.class && args[i] instanceof List) {
        convertedArgs[i] = args[i];
      } else {
        Converter converter = getConverter(argumentType);
        if (converter != null)
          convertedArgs[i] = converter.fromString((String) args[i]);
        else
          throw new SlimError(String.format("message:<<NO_CONVERTER_FOR_ARGUMENT_NUMBER %s.>>",
              argumentType.getName()));
      }
    }
    return convertedArgs;
  }

}
