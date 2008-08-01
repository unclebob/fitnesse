package fitnesse.revisioncontrol;

import fitnesse.wiki.WikiPage;


public interface State
{
    String REVISION_CONTROL_STATE = "RevisionControlState";

    RevisionControlOperation[] operations();

    boolean isNotUnderRevisionControl();

    String toString();

    boolean isCheckedOut();

    boolean isCheckedIn();

    void persist(WikiPage page) throws Exception;
}
