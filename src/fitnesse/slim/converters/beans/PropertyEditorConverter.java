package fitnesse.slim.converters.beans;

import fitnesse.slim.Converter;

import java.beans.PropertyEditor;

public class PropertyEditorConverter<T> implements Converter<T> {
  private PropertyEditor editor;

  public PropertyEditorConverter(PropertyEditor editor) {
    this.editor = editor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T fromString(String arg) {
    editor.setAsText(arg);
    return (T) editor.getValue();
  }

  @Override
  public String toString(Object o) {
    editor.setValue(o);
    return editor.getAsText();
  }
}
