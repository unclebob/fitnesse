package fitnesse.slim;

public interface NameTranslator {
  String translateClassName(String name);
  String translateMethodName(String name);
}
