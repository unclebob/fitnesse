package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import fitnesse.wiki.FileSystemPage;

public class RevertResponder extends RevisionControlResponder {
    public RevertResponder() {
        super(REVERT);
    }

    @Override
    protected void performOperation(FileSystemPage page) throws Exception {
        page.execute(REVERT);
    }
}
