package fitnesse.wiki;

import java.util.Optional;
import java.util.Properties;

import fitnesse.wikitext.VariableSource;

public class SystemVariableSource implements VariableSource {
  private final Properties properties;

  public SystemVariableSource(Properties properties) {
    this.properties = properties;
  }

  public SystemVariableSource() {
    this(null);
  }

  @Override
  public Optional<String> findVariable(String name) {
    return Optional.ofNullable(getProperty(name));
  }

  public String getProperty(String name) {
    String p = System.getenv(name);
    if (p != null) return p;

    p = System.getProperty(name);
    if (p != null) return p;

    return properties != null ? properties.getProperty(name) : null;
  }
}
