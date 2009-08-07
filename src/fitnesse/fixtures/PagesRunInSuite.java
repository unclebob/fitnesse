package fitnesse.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PagesRunInSuite {
  private String prefix;
  private String testSystemID;

  public PagesRunInSuite(String prefix, String testSystemID) {
    this.prefix = prefix;
    this.testSystemID = testSystemID;
  }

  public List<Object> query() throws Exception {
    return buildQueryResponse(getPagesRunInSuite());

  }

  private List<String> getPagesRunInSuite() throws Exception {
    List<String> testPages = new ArrayList<String>();

    String content = FitnesseFixtureContext.sender.sentData();
    String testSystems[] = content.split("slim:");
    for (String testSystem : testSystems) {
      addPagesForThisTestSystem(testPages, testSystem);
    }
    return testPages;
  }

  private void addPagesForThisTestSystem(List<String> testPages, String testSystem) {
    if (testSystem.startsWith(testSystemID)) {
      String pageString[] = testSystem.split("test_name\">");
      addPages(testPages, pageString);
    }
  }

  private void addPages(List<String> testPages, String[] pageString) {
    for (int i = 1; i < pageString.length; i++) {
      int fragmentEnd = pageString[i].indexOf("<");
      String testPage = pageString[i].substring(0, fragmentEnd);
      if (testPage.startsWith(prefix)) {
        testPages.add(testPage);
      }
    }
  }

  private List<Object> buildQueryResponse(List<String> testPages) {
    List<Object> rows = new ArrayList<Object>();
    for (int i = 0; i < testPages.size(); i++) {
      List<Object> columns = new ArrayList<Object>();
      List<String> pageNameCell = Arrays.asList("page name", testPages.get(i));
      columns.add(pageNameCell);
      rows.add(columns);
    }
    return rows;
  }
}
