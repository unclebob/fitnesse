package fitnesse.slim.converters;

import java.beans.PropertyEditor;

import fitnesse.slim.Converter;

public class PropertyEditorConverter implements Converter {
  private PropertyEditor editor;

  public PropertyEditorConverter(PropertyEditor editor ) {
    this.editor = editor;
  }

  public Object fromString(String arg) {
    editor.setAsText(arg);
    return editor.getValue();
  }

  public String toString(Object o) {
    editor.setValue(o);
    return editor.getAsText();
  }
}
