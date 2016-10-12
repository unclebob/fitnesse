// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;

public class MockCommandRunner extends CommandRunner {
  public MockCommandRunner(ExecutionLogListener executionLogListener) {
    super(new String[] {""}, null, executionLogListener, 2);
  }

  public MockCommandRunner(String commandText,
      ExecutionLogListener executionLogListener, int timeout) {
    super(new String[] { commandText }, null, executionLogListener, timeout);
  }

  @Override
  public void join() {
  }

  @Override
  public void kill() {
  }

  @Override
  public void asynchronousStart() throws IOException {
    sendCommandStartedEvent();
  }
}
