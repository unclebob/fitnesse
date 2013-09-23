package fitnesse.slim.test;

import java.beans.PropertyEditorSupport;

public class ZorkEditor extends PropertyEditorSupport {
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    String tokens[] = text.split("_");
    setValue(new Zork(Integer.parseInt(tokens[1])));
  }

  @Override
  public String getAsText() {
    Zork zork = (Zork)getValue();
    return String.format("zork_%d", zork.getInt());
  }
}
