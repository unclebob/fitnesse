package fitnesse.responders.revisioncontrol;

import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import fitnesse.revisioncontrol.RevisionControlException;

public class DeleteResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new DeleteResponder();
    }

    public void testShouldAskRevisionControllerToDeletePage() throws Exception {
        revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldDeleteAllChildPages() throws Exception {
        revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
        revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
        revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfDeleteOperationFails() throws Exception {
        final String errorMsg = "Cannot delete files from Revision Control";
        revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        expectLastCall().andThrow(new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }

    public void testAfterDeletingPageShouldProvideLinkToParentPage() throws Exception {
        revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
        replay(revisionController);

        createPage(FS_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString("Click <a href=\"" + FS_PARENT_PAGE + "\">here</a>", response.getContent());
    }

    public void testAfterDeletingTopMostPageShouldProvideLinkToWikiRootPage() throws Exception {
        revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString("Click <a href=\"\">here</a>", response.getContent());
    }

    public void testShouldReportErrorMsgIfChildPagesAreLockedOrCheckedOut() throws Exception {
        final String errorMsg = "Child Page cannot be deleted from Revision Control";
        revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
        expectLastCall().andThrow(new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }
}
