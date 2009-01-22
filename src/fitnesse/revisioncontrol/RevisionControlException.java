// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;

public class RevisionControlException extends Exception {
  private static final long serialVersionUID = 1L;

  public RevisionControlException(String errorMsg) {
    super(errorMsg);
  }

  public RevisionControlException(String errorMsg, Exception e) {
    super(errorMsg, e);
  }

}
