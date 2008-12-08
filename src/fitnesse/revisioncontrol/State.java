package fitnesse.revisioncontrol;

public interface State {
  String REVISION_CONTROL_STATE = "RevisionControlState";

  RevisionControlOperation[] operations();

  boolean isNotUnderRevisionControl();

  String toString();

  boolean isCheckedOut();

  boolean isCheckedIn();
}
