// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import static fitnesse.util.ListUtility.list;

import java.util.Date;
import java.util.List;

public class EmployeesHiredBefore {
  private Date date;

  public EmployeesHiredBefore(Date date) {
    this.date = date;
  }

  public List<Object> query() {

    return
      list(
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
