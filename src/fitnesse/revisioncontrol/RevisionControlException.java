package fitnesse.revisioncontrol;

public class RevisionControlException extends Exception
{

    public RevisionControlException(String errorMsg)
    {
        super(errorMsg);
    }

    public RevisionControlException(String errorMsg, Exception e)
    {
        super(errorMsg, e);
    }

}
