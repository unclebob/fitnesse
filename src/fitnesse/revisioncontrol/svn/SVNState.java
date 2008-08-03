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

    public static State checkState(String messageFromSVNClient) {
        for (final SVNState state : states.values())
            if (state.matchesSVNClientResponse(messageFromSVNClient))
                return state;
        return SVNState.UNKNOWN;
    }

    protected abstract boolean matchesSVNClientResponse(String messageFromSVNClient);

    protected boolean contains(String msg, String searchString) {
        return msg.indexOf(searchString) != -1;
    }
}

class Versioned extends SVNState {
    protected Versioned(String state) {
        super(state);
    }

    public RevisionControlOperation[] operations() {
        return new RevisionControlOperation[] { CHECKIN, UPDATE, REVERT, DELETE };
    }

    public boolean isNotUnderRevisionControl() {
        return false;
    }

    public boolean isCheckedIn() {
        return true;
    }

    @Override
    protected boolean matchesSVNClientResponse(String messageFromSVNClient) {
        return contains(messageFromSVNClient, "Schedule: normal");
    }
}

class Unknown extends SVNState {
    protected Unknown(String state) {
        super(state);
    }

    public RevisionControlOperation[] operations() {
        return new RevisionControlOperation[] { ADD };
    }

    public boolean isNotUnderRevisionControl() {
        return true;
    }

    public boolean isCheckedIn() {
        return false;
    }

    @Override
    protected boolean matchesSVNClientResponse(String messageFromSVNClient) {
        return contains(messageFromSVNClient, "Not a versioned resource") || contains(messageFromSVNClient, "is not a working copy");
    }
}

class Deleted extends SVNState {
    protected Deleted(String state) {
        super(state);
    }

    public RevisionControlOperation[] operations() {
        return new RevisionControlOperation[] { CHECKIN, REVERT };
    }

    public boolean isNotUnderRevisionControl() {
        return false;
    }

    public boolean isCheckedIn() {
        return true;
    }

    @Override
    protected boolean matchesSVNClientResponse(String messageFromSVNClient) {
        return contains(messageFromSVNClient, "Schedule: delete");
    }
}

class Added extends SVNState {
    protected Added(String state) {
        super(state);
    }

    public RevisionControlOperation[] operations() {
        return new RevisionControlOperation[] { CHECKIN, REVERT };
    }

    public boolean isNotUnderRevisionControl() {
        return true;
    }

    public boolean isCheckedIn() {
        return false;
    }

    @Override
    protected boolean matchesSVNClientResponse(String messageFromSVNClient) {
        return contains(messageFromSVNClient, "Schedule: add");
    }
}
