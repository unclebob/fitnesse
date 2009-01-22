// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;


public interface RevisionControllable {

  void execute(RevisionControlOperation operation) throws Exception;

  boolean isExternallyRevisionControlled();

  State checkState() throws Exception;

}
