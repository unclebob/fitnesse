package fitnesse.slim.test;

import util.ListUtility;

import static util.ListUtility.list;

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

  public Object query() {
      return "This is a test";
  }

  public List<Object> query2() {

    return
            ListUtility.<Object>list(
                    list(
                            list("employee number", "1429"),
                            list("first name", "Bob"),
                            list("last name", "Martin"),
                            list("hire date", "10-Oct-1974")
                    ),
                    list(
                            list("employee number", "8832"),
                            list("first name", "James"),
                            list("last name", "Grenning"),
                            list("hire date", "15-Dec-1979")
                    )
            );
  }
}
