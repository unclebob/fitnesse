package fitnesse.wikitext.shared;

import fitnesse.wikitext.VariableSource;

public interface PropertyStore extends PropertySource{
  void putProperty(String key, String value);

  default void copyVariables(String[] names, VariableSource source) {
    for (String name: names) {
      source.findVariable(name).ifPresent(value -> putProperty(name, value));
    }
  }
}
