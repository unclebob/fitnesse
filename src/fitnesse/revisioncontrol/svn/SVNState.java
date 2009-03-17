// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol.svn;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;

import java.util.HashMap;
import java.util.Map;

import fitnesse.revisioncontrol.RevisionControlOperation;
import fitnesse.revisioncontrol.State;

public abstract class SVNState implements State {
  String state;

  public static final SVNState VERSIONED = new Versioned("Versioned");
  public static final SVNState UNKNOWN = new Unknown("Unknown");
  public static final SVNState DELETED = new Deleted("Deleted");
  public static final SVNState ADDED = new Added("Added");
  private static final Map<String, SVNState> states = new HashMap<String, SVNState>();

  static {
    states.put("Versioned", VERSIONED);
    states.put("Unknown", UNKNOWN);
    states.put("Deleted", DELETED);
    states.put("Added", ADDED);
  }

  protected SVNState(String state) {
    this.state = state;
  }

  public boolean isCheckedOut() {
    return true;
  }

  @Override
  public String toString() {
    return state;
  }

  public static State instance(String state) {
    State revisionControlState = states.get(state);
    if (revisionControlState == null)
      revisionControlState = UNKNOWN;
    return revisionControlState;
  }

  protected boolean contains(String msg, String searchString) {
    return msg.indexOf(searchString) != -1;
  }
}

class Versioned extends SVNState {
  protected Versioned(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, UPDATE, REVERT, DELETE};
  }

  public boolean isNotUnderRevisionControl() {
    return false;
  }

  public boolean isCheckedIn() {
    return true;
  }
}

class Unknown extends SVNState {
  protected Unknown(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{ADD};
  }

  public boolean isNotUnderRevisionControl() {
    return true;
  }

  public boolean isCheckedIn() {
    return false;
  }

  @Override
  public boolean isCheckedOut() {
    return false;
  }
}

class Deleted extends SVNState {
  protected Deleted(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, REVERT};
  }

  public boolean isNotUnderRevisionControl() {
    return false;
  }

  public boolean isCheckedIn() {
    return true;
  }
}

class Added extends SVNState {
  protected Added(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, REVERT};
  }

  public boolean isNotUnderRevisionControl() {
    return true;
  }

  public boolean isCheckedIn() {
    return false;
  }
}
