package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import fitnesse.revisioncontrol.RevisionControlException;

public class CheckinResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new CheckinResponder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        verify(revisionController);
    }

    public void testShouldAskRevisionControllerToCheckinPage() throws Exception {
        expect(revisionController.execute(CHECKIN, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
        replay(revisionController);
        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfCheckinOperationFails() throws Exception {
        String errorMsg = "Cannot checkin files to Revision Control";
        expect(revisionController.execute(CHECKIN, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }
}
