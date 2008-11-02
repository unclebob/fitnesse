package fitnesse.revisioncontrol;


public interface RevisionControllable {

    void execute(RevisionControlOperation operation) throws Exception;

    boolean isExternallyRevisionControlled();

    State checkState() throws Exception;

}