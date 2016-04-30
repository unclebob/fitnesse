package fitnesse.wiki;

import java.util.Properties;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

public class SystemVariableSource implements VariableSource {
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
    return new Maybe<>(result);
  }

  public String getProperty(String name) {
    String p = System.getenv(name);
    if (p != null) return p;

    p = System.getProperty(name);
    if (p != null) return p;

    return properties != null ? properties.getProperty(name) : null;
  }
}
