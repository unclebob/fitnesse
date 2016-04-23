package fitnesse.wiki;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

public class ApplicationVariableSource implements VariableSource {

  private final VariableSource variableSource;

  public ApplicationVariableSource(VariableSource variableSource) {
    this.variableSource = variableSource;
  }

  @Override
  public Maybe<String> findVariable(String name) {
    String value;
    if (variableSource != null) {
      if (name.equals("FITNESSE_PORT")) {
        Maybe<String> port = variableSource.findVariable("FITNESSE_PORT");
        value = port.isNothing() ? "-1" : port.getValue();
      } else if (name.equals("FITNESSE_ROOTPATH")) {
        Maybe<String> path = variableSource.findVariable("FITNESSE_ROOTPATH");
        value = path.isNothing() ? "" : path.getValue();
      } else if (name.equals("FITNESSE_VERSION")) {
        Maybe<String> version = variableSource.findVariable("FITNESSE_VERSION");
        value = version.isNothing() ? "" : version.getValue();
      } else {
        return Maybe.noString;
      }
      return new Maybe<>(value);
    }
    return Maybe.noString;
  }
}
