package fitnesse.wikitext;

import java.util.Optional;

public class CompositeVariableSource implements VariableSource {

  private final VariableSource[] variableSources;

  public CompositeVariableSource(VariableSource... variableSources) {
    this.variableSources = variableSources;
  }

  @Override
  public Optional<String> findVariable(String name) {
    for (VariableSource variableSource : variableSources) {
      if (variableSource != null) {
        Optional<String> result = variableSource.findVariable(name);
        if (result.isPresent()) return result;
      }
    }
    return Optional.empty();
  }
}
