// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

public class ListExecutorTest extends ListExecutorTestBase {

  @Override
  protected ListExecutor getListExecutor() throws Exception {
    SlimFactory slimFactory = new JavaSlimFactory();
    return slimFactory.getListExecutor(false);
  }

  @Override
  protected String getTestClassPath() {
    return "fitnesse.slim.test";
  }
}
