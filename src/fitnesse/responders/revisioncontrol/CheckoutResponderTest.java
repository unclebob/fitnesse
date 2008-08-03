package fitnesse.responders.revisioncontrol;

import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import fitnesse.revisioncontrol.RevisionControlException;

public class CheckoutResponderTest extends RevisionControlTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        responder = new CheckoutResponder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        verify(revisionController);
    }

    public void testShouldAskRevisionControllerToCheckoutPage() throws Exception {
        revisionController.checkout(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        replay(revisionController);
        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
    }

    public void testAfterCheckoutShouldGiveEditLink() throws Exception {
        revisionController.checkout(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        replay(revisionController);
        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);
        invokeResponderAndCheckSuccessStatus();
        assertSubString("Click <a href=\"" + FS_PARENT_PAGE + "?edit\">here</a>", response.getContent());
    }

    public void testShouldReportErrorMsgIfCheckoutOperationFails() throws Exception {
        final String errorMsg = "Cannot checkout files to Revision Control";
        revisionController.checkout(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
        expectLastCall().andThrow(new RevisionControlException(errorMsg));
        replay(revisionController);

        createPage(FS_PARENT_PAGE);
        request.setResource(FS_PARENT_PAGE);

        invokeResponderAndCheckSuccessStatus();

        assertSubString(errorMsg, response.getContent());
    }
}
