// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.revisioncontrol.RevisionControlOperation.SYNC;
import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;

public class RevisionControlOperationTest {
  private final String filePath = "SomeFilePath";
  private final RevisionController revisionController = createMock(RevisionController.class);

  @Before
  public void init() {
    reset(revisionController);
  }

  @After
  public void verifyMocks() {
    verify(revisionController);
  }

  @Test
  public void shouldCreateActionHTMLLinkWithOperationDetails() throws Exception {
    replay(revisionController);

    final String pageName = "TestPage";
    HtmlTag actionLink = ADD.makeActionLink(pageName);
    assertEquals(link(ADD, pageName), actionLink.html());

    actionLink = DELETE.makeActionLink(pageName);
    assertEquals(link(DELETE, pageName), actionLink.html());
  }

  @Test
  public void addShouldDelegateCallToRevisionController() throws Exception {
    revisionController.add(filePath);
    replay(revisionController);
    ADD.execute(revisionController, filePath);
  }

  @Test
  public void checkinShouldDelegateCallToRevisionController() throws Exception {
    revisionController.checkin(filePath);
    replay(revisionController);
    CHECKIN.execute(revisionController, filePath);
  }

  @Test
  public void checkoutShouldDelegateCallToRevisionController() throws Exception {
    revisionController.checkout(filePath);
    replay(revisionController);
    CHECKOUT.execute(revisionController, filePath);
  }

  @Test
  public void deleteShouldDelegateCallToRevisionController() throws Exception {
    revisionController.delete(filePath);
    replay(revisionController);
    DELETE.execute(revisionController, filePath);
  }

  @Test
  public void revertShouldDelegateCallToRevisionController() throws Exception {
    revisionController.revert(filePath);
    replay(revisionController);
    REVERT.execute(revisionController, filePath);
  }

  @Test
  public void updateShouldDelegateCallToRevisionController() throws Exception {
    revisionController.update(filePath);
    replay(revisionController);
    UPDATE.execute(revisionController, filePath);
  }

  @Test
  public void syncShouldDelegateCallToRevisionController() throws Exception {
    expect(revisionController.checkState(filePath)).andReturn(VERSIONED);
    replay(revisionController);
    SYNC.execute(revisionController, filePath);
  }

  private String link(RevisionControlOperation operation, String pageName) {
    return "<!--" + operation.getName() + " button-->" + HtmlElement.endl + "<a href=\"" + pageName + "?" + operation.getQuery() + "\" accesskey=\""
      + operation.getAccessKey() + "\">" + operation.getName() + "</a>" + HtmlElement.endl;
  }

}
