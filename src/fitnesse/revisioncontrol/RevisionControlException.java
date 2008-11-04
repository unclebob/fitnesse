package fitnesse.revisioncontrol;

public class RevisionControlException extends Exception
{
    private static final long serialVersionUID = 1L;

    public RevisionControlException(String errorMsg)
    {
        super(errorMsg);
    }

    public RevisionControlException(String errorMsg, Exception e)
    {
        super(errorMsg, e);
    }

}
