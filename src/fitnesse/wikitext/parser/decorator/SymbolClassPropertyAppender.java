package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Symbol;

import static java.lang.String.format;


public class SymbolClassPropertyAppender {

  public static final String CLASS_PROPERTY_NAME = "class";

  private static final SymbolClassPropertyAppender INSTANCE = new SymbolClassPropertyAppender();

  public static SymbolClassPropertyAppender classPropertyAppender() {
    return INSTANCE;
  }

  private SymbolClassPropertyAppender() {
    //hidden
  }

  public void addPropertyValue(Symbol symbol, String propertyValue) {
    if (symbol.hasProperty(CLASS_PROPERTY_NAME)) {
      final String existingValue = symbol.getProperty(CLASS_PROPERTY_NAME);
      if (!alreadyContainsValue(existingValue, propertyValue)) {
        setPropertyValue(symbol, format("%s %s", existingValue, propertyValue));
      }
    } else {
      setPropertyValue(symbol, propertyValue);
    }
  }


  private void setPropertyValue(Symbol symbol, String propertyValue) {
    symbol.putProperty(CLASS_PROPERTY_NAME, propertyValue);
  }

  private boolean alreadyContainsValue(String existingValue, String valueToAdd) {
    return wrapInSpace(existingValue).contains(wrapInSpace(valueToAdd));
  }

  private String wrapInSpace(String existingValue) {
    return format(" %s ", existingValue);
  }
}
