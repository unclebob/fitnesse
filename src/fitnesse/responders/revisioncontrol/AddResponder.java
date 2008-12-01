package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import fitnesse.revisioncontrol.State;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;

public class AddResponder extends RevisionControlResponder {

  public AddResponder() {
    super(ADD);
  }

  @Override
  protected void beforeOperation(FileSystemPage page) throws Exception {
    final WikiPage parent = page.getParent();
    if (page == parent)
      return;
    if (page instanceof FileSystemPage) {
      final FileSystemPage parentPage = (FileSystemPage) parent;
      final State currentState = parentPage.checkState();
      if (currentState == null || currentState.isNotUnderRevisionControl())
        executeRevisionControlOperation(parentPage);
    }
  }

  @Override
  protected void performOperation(FileSystemPage page) throws Exception {
    page.execute(ADD);
  }

}
