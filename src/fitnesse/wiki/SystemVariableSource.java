package fitnesse.wiki;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.Maybe;

public class SystemVariableSource implements VariableSource, Serializable {
  private final Properties properties;
  private Map<String,Object> testUrlParams;

  public SystemVariableSource(Properties properties) {
    this.properties = properties;
    this.testUrlParams = null;
  }

  public SystemVariableSource() {
    this(null);
  }

  public void setUrlParams(Map<String,Object> testUrlParams){
      this.testUrlParams = testUrlParams;
  }

  public Map<String,Object> getUrlParams(){
      return this.testUrlParams;
  }

  @Override
  public Maybe<String> findVariable(String name) {
    String result = getProperty(name);
    if (result == null) return Maybe.noString;
    return new Maybe<String>(result);
  }

  public String getProperty(String name) {
    if(testUrlParams != null && testUrlParams.containsKey(name)) { return (String)testUrlParams.get(name); }

    String p = System.getenv(name);
    if (p != null) return p;

    p = System.getProperty(name);
    if (p != null) return p;

    return properties != null ? properties.getProperty(name) : null;
  }
}