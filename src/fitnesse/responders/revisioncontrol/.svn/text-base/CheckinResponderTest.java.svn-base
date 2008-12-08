package fitnesse.responders.revisioncontrol;

import fitnesse.revisioncontrol.RevisionControlException;
import static fitnesse.testutil.RegexTestCase.assertSubString;
import static org.easymock.EasyMock.*;

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
    revisionController.checkin(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    replay(revisionController);
    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);
    invokeResponderAndCheckSuccessStatus();
  }

  public void testShouldReportErrorMsgIfCheckinOperationFails() throws Exception {
    final String errorMsg = "Cannot checkin files to Revision Control";
    revisionController.checkin(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    expectLastCall().andThrow(new RevisionControlException(errorMsg));
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, response.getContent());
  }
}
