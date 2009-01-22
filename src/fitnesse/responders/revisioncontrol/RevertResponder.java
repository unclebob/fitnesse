// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import fitnesse.wiki.FileSystemPage;

public class RevertResponder extends RevisionControlResponder {
  public RevertResponder() {
    super(REVERT);
  }

  @Override
  protected void performOperation(FileSystemPage page) throws Exception {
    page.execute(REVERT);
  }
}
