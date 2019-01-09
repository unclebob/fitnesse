package fitnesse.slim.converters;

/**
 * This interface was created to replace {@link java.beans.PropertyEditor} for Android
 */
public interface PropertyEditor {
  void setAsText(String text);

  String getAsText();

  void setValue(Object value);

  Object getValue();
}
