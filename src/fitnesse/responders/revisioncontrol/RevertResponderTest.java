package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import fitnesse.revisioncontrol.RevisionControlException;

public class RevertResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new RevertResponder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        verify(revisionController);
    }

    public void testShouldAskRevisionControllerToRevertPage() throws Exception {
        expect(revisionController.execute(REVERT, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfRevertOperationFails() throws Exception {
        String errorMsg = "Cannot revert files from Revision Control";
        expect(revisionController.execute(REVERT, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }

    public void testShouldOnlyRevertCurrentPage() throws Exception {
        expect(revisionController.execute(REVERT, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(VERSIONED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }
}
