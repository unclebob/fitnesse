package fitnesse.slim.test;

import util.ListUtility;

import static util.ListUtility.list;

import java.util.Date;
import java.util.List;

public class EmployeesHiredBefore {

  public EmployeesHiredBefore(Date date) {
  }

  public void table(List<List<String>> table) {
    //optional function
  }

  public List<Object> query() {

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
