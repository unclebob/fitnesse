package fitnesse.slim.test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    return Arrays.asList(Arrays.asList(Arrays.asList("company number", "4808147"), Arrays.asList("employee number", "1429"), Arrays.asList("first name", "Bob"), Arrays.asList("last name", "Martin"), Arrays.asList("hire date", "10-Oct-1974")), Arrays.asList(Arrays.asList("company number", "5123122"), Arrays.asList("employee number", "8832"), Arrays.asList("first name", "James"), Arrays.asList("last name", "Grenning"), Arrays.asList("hire date", "15-Dec-1979")));
  }
}
