package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.ADDED;
import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.STATE;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import fitnesse.revisioncontrol.RevisionControlException;

public class AddResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new AddResponder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        verify(revisionController);
    }

    public void testShouldAskRevisionControllerToAddPage() throws Exception {
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(ADDED);
        replay(revisionController);
        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldReportErrorMsgIfAddOperationFails() throws Exception {
        String errorMsg = "Cannot add files to Revision Control";
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(
                new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }

    public void testShouldSkipAddingFilesIfTheyAreAlreadyUnderRevisionControl() throws Exception {
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(ADDED);
        replay(revisionController);
        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testShouldAskRevisionControllerToAddAllParentPages() throws Exception {
        expect(revisionController.execute(STATE, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(UNKNOWN);
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(ADDED);
        expect(revisionController.execute(STATE, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(UNKNOWN);
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(ADDED);
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(ADDED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }

    public void testParentRemainsInSameStateIfAlreadyUnderRevisionControl() throws Exception {
        expect(revisionController.execute(STATE, contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
        expect(revisionController.execute(STATE, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(UNKNOWN);
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(ADDED);
        expect(revisionController.execute(ADD, contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(ADDED);
        replay(revisionController);

        createPage(FS_GRAND_CHILD_PAGE);
        request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

        invokeResponderAndCheckSuccessStatus();
    }
}
