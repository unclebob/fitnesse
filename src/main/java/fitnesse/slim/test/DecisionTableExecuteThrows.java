// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import fitnesse.slim.SlimError;

public class DecisionTableExecuteThrows {
  public int x() {
    return 1;
  }

  public void execute() {
    throw new SlimError("EXECUTE_THROWS");
  }
}
