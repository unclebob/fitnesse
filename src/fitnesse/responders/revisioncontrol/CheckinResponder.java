package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import fitnesse.wiki.FileSystemPage;

public class CheckinResponder extends RevisionControlResponder {

    public CheckinResponder() {
        super(CHECKIN);
    }

    @Override
    protected void performOperation(FileSystemPage page) throws Exception {
        page.execute(CHECKIN);
    }

}
