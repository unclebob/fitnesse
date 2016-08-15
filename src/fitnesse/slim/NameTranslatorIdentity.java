package fitnesse.slim;

public class NameTranslatorIdentity implements NameTranslator {

  @Override
  public String translateClassName(String name) {
    return name;
  }

  @Override
  public String translateMethodName(String name) {
    return name;
  }

}
