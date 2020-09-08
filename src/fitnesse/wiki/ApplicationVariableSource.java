package fitnesse.wiki;

import fitnesse.wikitext.VariableSource;

import java.util.Optional;

public class ApplicationVariableSource implements VariableSource {

  private final VariableSource variableSource;

  public ApplicationVariableSource(VariableSource variableSource) {
    this.variableSource = variableSource;
  }

  @Override
  public Optional<String> findVariable(String name) {
    if (variableSource != null) {
      switch (name) {
        case "FITNESSE_PORT":
          return Optional.of(variableSource.findVariable("FITNESSE_PORT").orElse("-1"));
        case "FITNESSE_ROOTPATH":
          return Optional.of(variableSource.findVariable("FITNESSE_ROOTPATH").orElse(""));
        case "FITNESSE_VERSION":
          return Optional.of(variableSource.findVariable("FITNESSE_VERSION").orElse(""));
        default:
          return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
