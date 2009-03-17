// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static util.RegexTestCase.assertSubString;
import fitnesse.revisioncontrol.RevisionControlException;

public class DeleteResponderTest extends RevisionControlTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.responder = new DeleteResponder();
  }

  public void testShouldAskRevisionControllerToDeletePage() throws Exception {
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    replay(this.revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    this.request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertPageDoesNotExists(FS_GRAND_CHILD_PAGE);
  }

  public void testShouldRemovePageReferenceFromParentAfterDeletingChildPage() throws Exception {
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    replay(this.revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    this.request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();
    assertNull("Parent page had a reference to child file", this.childPage.getChildPage(FS_GRAND_CHILD_PAGE));
    assertPageDoesNotExists(FS_GRAND_CHILD_PAGE);
  }

  public void testShouldDeleteAllChildPages() throws Exception {
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    replay(this.revisionController);

    createPage(FS_GRAND_CHILD_PAGE);
    this.request.setResource(FS_PARENT_PAGE);
    invokeResponderAndCheckSuccessStatus();
    assertPageDoesNotExists(FS_GRAND_CHILD_PAGE);
    assertPageDoesNotExists(FS_CHILD_PAGE);
    assertPageDoesNotExists(FS_PARENT_PAGE);
  }

  public void testShouldReportErrorMsgIfDeleteOperationFails() throws Exception {
    final String errorMsg = "Cannot delete files from Revision Control";
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    expectLastCall().andThrow(new RevisionControlException(errorMsg));
    replay(this.revisionController);

    createPage(FS_PARENT_PAGE);
    this.request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, this.response.getContent());
  }

  public void testAfterDeletingPageShouldProvideLinkToParentPage() throws Exception {
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    replay(this.revisionController);

    createPage(FS_CHILD_PAGE);
    this.request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString("Click <a href=\"" + FS_PARENT_PAGE + "\">here</a>", this.response.getContent());
  }

  public void testAfterDeletingTopMostPageShouldProvideLinkToWikiRootPage() throws Exception {
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_PARENT_PAGE));
    replay(this.revisionController);

    createPage(FS_PARENT_PAGE);
    this.request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString("Click <a href=\"\">here</a>", this.response.getContent());
  }

  public void testShouldReportErrorMsgIfChildPagesAreLockedOrCheckedOut() throws Exception {
    final String errorMsg = "Child Page cannot be deleted from Revision Control";
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_CHILD_PAGE));
    expectLastCall().andThrow(new RevisionControlException(errorMsg));
    replay(this.revisionController);

    createPage(FS_CHILD_PAGE);
    this.request.setResource(FS_PARENT_PAGE);

    invokeResponderAndCheckSuccessStatus();

    assertSubString(errorMsg, this.response.getContent());
  }
}
