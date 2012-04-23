// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

public interface TestSystemListener {
  public void acceptOutputFirst(String output) throws IOException;

  public void testComplete(TestSummary testSummary) throws IOException;

  public void exceptionOccurred(Throwable e);
}
