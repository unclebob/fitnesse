package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;
import fitnesse.wiki.FileSystemPage;

public class UpdateResponder extends RevisionControlResponder {
    public UpdateResponder() {
        super(UPDATE);
    }

    @Override
    protected void performOperation(FileSystemPage page) throws Exception {
        page.execute(UPDATE);
    }
}
