// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;

public interface TestSystem {

  String getName();

  void start() throws IOException;

  void bye() throws IOException, InterruptedException;

  void kill() throws IOException;

  void runTests(TestPage pageToTest) throws IOException, InterruptedException;

  boolean isSuccessfullyStarted();
}
