package fitnesse.wiki;

import java.io.Serializable;
import java.util.Properties;

import fitnesse.FitNesseContext;
import fitnesse.wikitext.parser.VariableSource;
import util.Maybe;

public class SystemVariableSource implements VariableSource, Serializable {
  private final Properties properties;

  public SystemVariableSource(Properties properties) {
    this.properties = properties;
  }

  public SystemVariableSource() {
    this(null);
  }

  @Override
  public Maybe<String> findVariable(String name) {
    String result = getProperty(name);
    if (result == null) return Maybe.noString;
    return new Maybe<String>(result);
  }

  public String getProperty(String name) {
    String p = System.getenv(name);
    if (p != null) return p;

    p = System.getProperty(name);
    if (p != null) return p;

    return properties != null ? properties.getProperty(name) : null;
  }
}