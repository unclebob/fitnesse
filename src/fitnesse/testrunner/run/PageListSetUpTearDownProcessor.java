package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.List;

public interface PageListSetUpTearDownProcessor {
  List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pageList);
}
