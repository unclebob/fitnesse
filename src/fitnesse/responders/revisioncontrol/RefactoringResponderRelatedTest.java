// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import fitnesse.responders.refactoring.DeletePageResponder;

public class RefactoringResponderRelatedTest extends RevisionControlTestCase {
  public void testShouldDeleteVersionedPageFromRevisionControll() throws Exception {
    super.setUp();
    this.responder = new DeletePageResponder();
    expect(this.revisionController.checkState(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(VERSIONED);
    this.revisionController.delete(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE));
    replay(this.revisionController);

    createPage(FS_GRAND_CHILD_PAGE);

    this.request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);
    this.request.addInput("confirmed", "yes");

    invokeResponderAndCheckStatusIs(303);

    assertPageDoesNotExists(FS_GRAND_CHILD_PAGE);
  }

  public void testShouldNotDeleteNonVersionedPageFromRevisionControll() throws Exception {
    this.responder = new DeletePageResponder();
    expect(this.revisionController.checkState(contentAndPropertiesFilePathFor(FS_GRAND_CHILD_PAGE))).andReturn(UNKNOWN);
    replay(this.revisionController);

    createPage(FS_GRAND_CHILD_PAGE);

    this.request.setResource(FS_PARENT_PAGE + "." + FS_CHILD_PAGE + "." + FS_GRAND_CHILD_PAGE);
    this.request.addInput("confirmed", "yes");

    invokeResponderAndCheckStatusIs(303);

    assertPageDoesNotExists(FS_GRAND_CHILD_PAGE);
  }
}
