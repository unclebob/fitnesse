// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import fitnesse.responders.run.TestSummary;

public interface ResultHandler {
  void acceptResult(PageResult result) throws Exception;

  void acceptFinalCount(TestSummary testSummary) throws Exception;
}
