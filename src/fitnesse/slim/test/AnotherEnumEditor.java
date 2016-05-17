package fitnesse.slim.test;

import java.beans.PropertyEditorSupport;

public class AnotherEnumEditor extends PropertyEditorSupport {

  @Override
  public void setAsText(String value) {
    setValue("enum property editor called with \"" + value + "\"");
  }

  @Override
  public String getAsText() {
    String value = (String) getValue();
    return value;
  }

}
