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
    List<String> testPages = new ArrayList<String>();

    String content = FitnesseFixtureContext.sender.sentData();
    String testSystems[] = content.split("slim:");
    for (String testSystem : testSystems) {
      if (testSystem.startsWith(testSystemID)) {
        String fragments[] = testSystem.split("test_name\">");
        for (int i = 1; i < fragments.length; i++) {
          int fragmentEnd = fragments[i].indexOf("<");
          String testPage = fragments[i].substring(0, fragmentEnd);
          if (testPage.startsWith(prefix)) {
            testPages.add(testPage);
          }
        }
      }
    }

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
