// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static util.RegexTestCase.assertSubString;
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
    revisionController.add(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    replay(revisionController);
    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);
    invokeResponderAndCheckSuccessStatus();
  }

  public void testShouldReportErrorMsgIfAddOperationFails() throws Exception {
    final String errorMsg = "Cannot add files to Revision Control";
    revisionController.add(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    expectLastCall().andThrow(new RevisionControlException(errorMsg));
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, response.getContent());
  }

  public void testShouldAskRevisionControllerToAddAllParentPages() throws Exception {
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(UNKNOWN);
    revisionController.add(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(UNKNOWN);
    revisionController.add(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    revisionController.add(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    replay(revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }

  public void testParentRemainsInSameStateIfAlreadyUnderRevisionControl() throws Exception {
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(UNKNOWN);
    revisionController.add(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    revisionController.add(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    replay(revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }
}
