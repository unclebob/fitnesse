package fitnesse.slim.test;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Used by FitNesse.UserGuide.SliM.QueryTable.
 */
public class EmployeesHiredBefore {

  public EmployeesHiredBefore(Date date) {
  }

  public void table(List<List<String>> table) {
    //optional function
  }

  public List<List<List<String>>> query() {

    return
      asList( // table level
        asList( // row level
          asList("company number", "4808147"), // cell column name, value
          asList("employee number", "1429"),
          asList("first name", "Bob"),
          asList("last name", "Martin"),
          asList("hire date", "10-Oct-1974")
        ),
        asList(
          asList("company number", "5123122"),
          asList("employee number", "8832"),
          asList("first name", "James"),
          asList("last name", "Grenning"),
          asList("hire date", "15-Dec-1979")
        )
      );
  }
}
