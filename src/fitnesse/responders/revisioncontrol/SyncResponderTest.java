package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.revisioncontrol.RevisionControlOperation.SYNC;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import fitnesse.revisioncontrol.RevisionControlException;

public class SyncResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new SyncResponder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        verify(revisionController);
    }

    public void testShouldAskRevisionControllerToSyncronizePage() throws Exception {
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfSyncronizationFails() throws Exception {
        String errorMsg = "Cannot synchronize files from Revision Control";
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }

    public void testShouldSyncronizeAllChildPage() throws Exception {
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(VERSIONED);
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(VERSIONED);
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldStopSyncronizationIfAnyChildPageThrowErrors() throws Exception {
        String errorMsg = "Some error";
        expect(revisionController.execute(SYNC, contentAndPropertiesFilePathFor(FS_SIBLING_CHILD_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_CHILD_PAGE);
        createPage(FS_SIBLING_CHILD_PAGE, parentPage);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }
}
