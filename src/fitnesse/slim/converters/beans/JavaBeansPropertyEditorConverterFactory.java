package fitnesse.slim.converters.beans;

import fitnesse.slim.Converter;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

/**
 * Class to provide Converters based on JavaBeans PropertyEditors.
 *
 * (This class should not be referenced directly since java.beans package
 * is not available on Android. It should only be called via reflection)
 */
public class JavaBeansPropertyEditorConverterFactory {
  public static <T> Converter<T> getConverter(Class<?> clazz) {
    PropertyEditor pe = PropertyEditorManager.findEditor(clazz);
    if (pe != null && !"EnumEditor".equals(pe.getClass().getSimpleName())) {
      // com.sun.beans.EnumEditor and sun.beans.EnumEditor seem to be used in different usages.
      return new PropertyEditorConverter<>(pe);
    }
    return null;
  }

  public static class PropertyEditorConverter<T> implements Converter<T> {
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
}
