package fitnesse.wiki;

import java.util.Map;

import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.Maybe;

public class UrlPathVariableSource implements VariableSource {
  private final SystemVariableSource systemVariables;
  private final Map<String,Object> urlParams;

  public UrlPathVariableSource(SystemVariableSource systemVariables, Map<String,Object> urlParams) {
    this.systemVariables = systemVariables;
    this.urlParams = urlParams;
  }

  @Override
  public Maybe<String> findVariable(String name) {
    if(urlParams != null && urlParams.containsKey(name)) { 
        return new Maybe<String>((String)urlParams.get(name)); 
    }
    
    return systemVariables.findVariable(name);
  }
  
  public Map<String, Object> getUrlParams() {
      return urlParams;
  }
}