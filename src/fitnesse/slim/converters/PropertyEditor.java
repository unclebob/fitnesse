package fitnesse.slim.converters;

/**
 * This interface was created to replace {@link java.beans.PropertyEditor} for Android
 */
public interface PropertyEditor {
    public void setAsText(String text);

    public String getAsText();

    public void setValue(Object value);

    public Object getValue();
}
