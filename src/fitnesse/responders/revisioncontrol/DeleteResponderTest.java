package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.DELETED;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import fitnesse.revisioncontrol.RevisionControlException;

public class DeleteResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new DeleteResponder();
    }

    public void testShouldAskRevisionControllerToDeletePage() throws Exception {
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(DELETED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldDeleteAllChildPages() throws Exception {
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(DELETED);
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(DELETED);
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(DELETED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfDeleteOperationFails() throws Exception {
        String errorMsg = "Cannot delete files from Revision Control";
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }

    public void testAfterDeletingPageShouldProvideLinkToParentPage() throws Exception {
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(DELETED);
        replay(revisionController);

        createPage(FS_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString("Click <a href=\"" + FS_PARENT_PAGE + "\">here</a>", response.getContent());
    }

    public void testAfterDeletingTopMostPageShouldProvideLinkToWikiRootPage() throws Exception {
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(DELETED);
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString("Click <a href=\"\">here</a>", response.getContent());
    }

    public void testShouldReportErrorMsgIfChildPagesAreLockedOrCheckedOut() throws Exception {
        String errorMsg = "Child Page cannot be deleted from Revision Control";
        expect(revisionController.execute(DELETE, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }
}
