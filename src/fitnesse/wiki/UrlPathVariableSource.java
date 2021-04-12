package fitnesse.wiki;

import java.util.Map;
import java.util.Optional;

import fitnesse.wikitext.VariableSource;
import fitnesse.wikitext.parser.Maybe;

public class UrlPathVariableSource implements VariableSource {
  private final VariableSource systemVariables;
  private final Map<String,String> urlParams;

  public UrlPathVariableSource(VariableSource systemVariables, Map<String,String> urlParams) {
    this.systemVariables = systemVariables;
    this.urlParams = urlParams;
  }

  @Override
  public Optional<String> findVariable(String name) {
    if(urlParams != null && urlParams.containsKey(name)) {
        return Optional.ofNullable(urlParams.get(name));
    }

    return systemVariables.findVariable(name);
  }

  public Maybe<String> findUrlVariable(String name) {
    if(urlParams != null && urlParams.containsKey(name)) {
      return new Maybe<>(urlParams.get(name));
    }

    return Maybe.noString;
  }

}
