package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class ElementConverterHelper {
  public static String elementToString(Object elementValue) {
    String valueToAdd = "null";
    if (elementValue != null) {
      Converter converter = ConverterRegistry.getConverterForClass(elementValue.getClass());
      String convertedValue;
      if (converter == null) {
        convertedValue = elementValue.toString();
      } else {
        convertedValue = converter.toString(elementValue);
      }
      if (convertedValue != null) {
        valueToAdd = convertedValue;
      }
    }
    return valueToAdd;
  }
}
