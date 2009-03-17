// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;

public abstract class NullState implements State {
  protected String state;

  public static final NullState VERSIONED = new Versioned("Versioned");
  public static final NullState UNKNOWN = new Unknown("Unknown");
  public static final NullState DELETED = new Deleted("Deleted");
  public static final NullState ADDED = new Added("Added");

  protected NullState(String state) {
    this.state = state;
  }

  public boolean isCheckedOut() {
    return true;
  }

}

class Versioned extends NullState {
  protected Versioned(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKOUT, DELETE, UPDATE};
  }

  public boolean isNotUnderRevisionControl() {
    return false;
  }

  public boolean isCheckedIn() {
    return true;
  }
}

class Unknown extends NullState {
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
}

class Deleted extends NullState {
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

class Added extends NullState {
  protected Added(String state) {
    super(state);
  }

  public RevisionControlOperation[] operations() {
    return new RevisionControlOperation[]{CHECKIN, REVERT};
  }

  public boolean isNotUnderRevisionControl() {
    return false;
  }

  public boolean isCheckedIn() {
    return false;
  }
}
