package fitnesse.slim.converters;

/**
 *  This interface was created to replace {@link java.beans.PropertyEditorSupport} for Android.
 */
public class PropertyEditorSupport implements PropertyEditor {
    Object source = null;

    Object oldValue = null;

    Object newValue = null;

    public PropertyEditorSupport(Object source) {
        this.source = source;
    }

    public PropertyEditorSupport() {
        source = this;
    }


    public void setAsText(String text) throws IllegalArgumentException {
        if (newValue instanceof String) {
            setValue(text);
        } else {
            throw new IllegalArgumentException(text);
        }
    }


    public String getAsText() {
        return newValue == null ? "null" : newValue.toString(); //$NON-NLS-1$
    }

    public void setValue(Object value) {
        this.oldValue = this.newValue;
        this.newValue = value;
    }

    public Object getValue() {
        return newValue;
    }


    public Object getSource() {
        return source;
    }

}
