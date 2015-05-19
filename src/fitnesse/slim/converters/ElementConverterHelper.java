package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class ElementConverterHelper {
  public static String elementToString(Object cellValue) {
    String valueToAdd = "null";
    if (cellValue != null) {
      Converter converter = ConverterRegistry.getConverterForClass(cellValue.getClass());
      String convertedValue;
      if (converter == null) {
        convertedValue = cellValue.toString();
      } else {
        convertedValue = converter.toString(cellValue);
      }
      if (convertedValue != null) {
        valueToAdd = convertedValue;
      }
    }
    return valueToAdd;
  }
}
