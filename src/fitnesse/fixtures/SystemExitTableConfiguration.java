package fitnesse.fixtures;


public class SystemExitTableConfiguration {

  public boolean setSystemPropertyTo(String systemProperty, String value) {
    String originalSystemProperty = System.getProperty(systemProperty);
    if (originalSystemProperty != null){
      System.setProperty(systemProperty + ".orig", originalSystemProperty);
    }
    System.setProperty(systemProperty, value);
    return true;
  }

  public boolean restoreOriginalSystemProperty(String systemProperty) {
    String originalSystemProperty = System.getProperty(systemProperty + ".orig");
    if (originalSystemProperty == null) {
      System.clearProperty(systemProperty);
    } else {
      System.setProperty(systemProperty, originalSystemProperty);
    }
    System.clearProperty(systemProperty + ".orig");
    return true;
  }

}
