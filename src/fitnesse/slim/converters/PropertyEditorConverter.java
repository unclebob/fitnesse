package fitnesse.slim.converters;

import java.beans.PropertyEditor;

import fitnesse.slim.Converter;

public class PropertyEditorConverter<T> implements Converter<T> {
  private PropertyEditor editor;

  public PropertyEditorConverter(PropertyEditor editor) {
    this.editor = editor;
  }

  @SuppressWarnings("unchecked")
  public T fromString(String arg) {
    editor.setAsText(arg);
    return (T) editor.getValue();
  }

  public String toString(Object o) {
    editor.setValue(o);
    return editor.getAsText();
  }
}
