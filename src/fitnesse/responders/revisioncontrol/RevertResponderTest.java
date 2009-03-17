// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static util.RegexTestCase.assertSubString;
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
    revisionController.revert(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }

  public void testShouldReportErrorMsgIfRevertOperationFails() throws Exception {
    final String errorMsg = "Cannot revert files from Revision Control";
    revisionController.revert(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    expectLastCall().andThrow(new RevisionControlException(errorMsg));
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, response.getContent());
  }

  public void testShouldOnlyRevertCurrentPage() throws Exception {
    revisionController.revert(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    replay(revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }
}
