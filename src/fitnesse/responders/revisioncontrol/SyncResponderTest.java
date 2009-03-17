// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static util.RegexTestCase.assertSubString;
import fitnesse.revisioncontrol.RevisionControlException;

public class SyncResponderTest extends RevisionControlTestCase {
  public void testShouldAskRevisionControllerToSyncronizePage() throws Exception {
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }

  public void testShouldReportErrorMsgIfSyncronizationFails() throws Exception {
    final String errorMsg = "Cannot synchronize files from Revision Control";
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andThrow(new RevisionControlException(errorMsg));
    replay(revisionController);

    createPage(FS_PARENT_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, response.getContent());
  }

  public void testShouldStopSyncronizationIfAnyChildPageThrowErrors() throws Exception {
    final String errorMsg = "Some error";
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_SIBLING_CHILD_PAGE))).andThrow(new RevisionControlException(errorMsg));
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(VERSIONED).anyTimes();
    replay(revisionController);

    createPage(FS_CHILD_PAGE);
    createPage(FS_SIBLING_CHILD_PAGE, parentPage);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, response.getContent());
  }

  public void testShouldSyncronizeAllChildPage() throws Exception {
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(VERSIONED);
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_CHILD_PAGE))).andReturn(VERSIONED);
    expect(revisionController.checkState(contentAndPropertiesFilePathFor(FS_PARENT_PAGE))).andReturn(VERSIONED);
    replay(revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();
  }

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
}
