package fitnesse.slim;

public class NameTranslatorIdentity implements NameTranslator {

  @Override
  public String translate(String name) {
    return name;
  }

}
