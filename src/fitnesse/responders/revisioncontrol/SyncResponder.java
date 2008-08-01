package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.SYNC;

import java.util.List;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;

public class SyncResponder extends RevisionControlResponder {
    public SyncResponder() {
        super(SYNC);
    }

    @Override
    protected void beforeOperation(FileSystemPage page) throws Exception {
        List<WikiPage> children = page.getChildren();
        for (WikiPage child : children) {
            if (child instanceof FileSystemPage) {
                executeRevisionControlOperation((FileSystemPage) child);
            }
        }
    }

    @Override
    protected void performOperation(FileSystemPage page) throws Exception {
        page.execute(SYNC);
    }

}
