package fitnesse.testsystems;


import java.util.List;

public interface TestPage {

  String getHtml();

  String getVariable(String name);

  String getFullPath();

  ClassPath getClassPath();
}
