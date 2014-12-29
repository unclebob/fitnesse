package fitnesse.testsystems;

public interface TestPage {

  String getHtml();

  String getVariable(String name);

  String getFullPath();

  ClassPath getClassPath();
}
