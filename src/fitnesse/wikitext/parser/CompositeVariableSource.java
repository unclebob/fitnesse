package fitnesse.wikitext.parser;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

public class CompositeVariableSource implements VariableSource {

  private final VariableSource[] variableSources;

  public CompositeVariableSource(VariableSource... variableSources) {
    this.variableSources = variableSources;
  }

  @Override
  public Maybe<String> findVariable(String name) {
    for (VariableSource variableSource : variableSources) {
      if (variableSource != null) {
        Maybe<String> result = variableSource.findVariable(name);
        if (!result.isNothing()) return result;
      }
    }
    return Maybe.noString;
  }
}
