package fitnesse.wikitext;

public interface VariableStore extends VariableSource {
  void putVariable(String name, String value);
  int nextId();
}
