package fitnesse.wiki;

import java.util.Map;

import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.Maybe;

public class UrlPathVariableSource implements VariableSource {
  private final VariableSource systemVariables;
  private final Map<String,String> urlParams;

  public UrlPathVariableSource(VariableSource systemVariables, Map<String,String> urlParams) {
    this.systemVariables = systemVariables;
    this.urlParams = urlParams;
  }

  @Override
  public Maybe<String> findVariable(String name) {
    if(urlParams != null && urlParams.containsKey(name)) { 
        return new Maybe<String>(urlParams.get(name));
    }
    
    return systemVariables.findVariable(name);
  }

  public Maybe<String> findUrlVariable(String name) {
    if(urlParams != null && urlParams.containsKey(name)) {
      return new Maybe<String>((String)urlParams.get(name));
    }

    return Maybe.noString;
  }

}