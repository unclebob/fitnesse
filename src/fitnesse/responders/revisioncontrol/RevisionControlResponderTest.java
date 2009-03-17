// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashSet;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.revisioncontrol.NullState;
import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionControlOperation;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;

public class RevisionControlResponderTest extends RevisionControlTestCase {
  private final String revisionControlOperation = "Test Revision Control Operation";
  private static final String pageName = "SomePage";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.root = InMemoryPage.makeRoot("RooT");
    this.context = new FitNesseContext(this.root);
    this.request = new MockRequest();
    this.responder = new TestRevisionControlResponder();
  }

  public void testShouldReturnPageNotFoundMessageWhenPageDoesNotExist() throws Exception {
    replay(this.revisionController);
    final String pageName = "InvalidPageName";
    this.request.setResource(pageName);
    invokeResponderAndCheckResponseContains("The requested resource: <i>" + pageName + "</i> was not found.");
  }

  public void testShouldReturnInvalidWikiPageMessageIfWikiPageDoesNotExistOnFileSystem() throws Exception {
    replay(this.revisionController);
    final String inMemoryPageName = "InMemoryPage";
    this.root.addChildPage(inMemoryPageName);
    this.request.setResource(inMemoryPageName);
    invokeResponderAndCheckResponseContains("The page " + inMemoryPageName + " doesn't support '" + this.revisionControlOperation + "' operation.");
  }

  public void testShouldResolveSymbolicLinkToActualPageAndApplyRevisionControlOperations() throws Exception {
    replay(this.revisionController);
    final String symbolicLinkName = "SymbolicLink";
    final String pageOneName = "PageOne";
    final String symbolicLinkPageName = pageOneName + "." + symbolicLinkName;
    createSymbolicLink(symbolicLinkName, pageOneName);

    this.request.setResource(symbolicLinkPageName);
    invokeResponderAndCheckResponseContains("The page " + symbolicLinkPageName + " doesn't support '" + this.revisionControlOperation + "' operation.");
  }

  public void testShouldReportPerformRevisionControlOperation() throws Exception {
    final String expectedResponse = "Attempted to '" + this.revisionControlOperation + "' the page '" + pageName
      + "'. The result was:<br/><br/><pre>Operation: '" + this.revisionControlOperation + "' was successful.";
    this.revisionController = createNiceMock(RevisionController.class);
    expect(this.revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
    expect(this.revisionController.checkState((String) anyObject())).andStubReturn(NullState.UNKNOWN);
    replay(this.revisionController);
    createExternalRoot();
    this.root.getPageCrawler().addPage(this.root, PathParser.parse(pageName), "Test Page Content");
    this.request.setResource(pageName);

    invokeResponderAndCheckResponseContains(expectedResponse);
    verify(this.revisionController);
  }

  private void createSymbolicLink(final String symbolicLinkName, final String pageOneName) throws Exception {
    final String pageTwoName = "PageTwo";
    final WikiPage pageOne = this.root.addChildPage(pageOneName);
    this.root.addChildPage(pageTwoName);

    final PageData data = pageOne.getData();
    final WikiPageProperties properties = data.getProperties();
    final WikiPageProperty symLinks = getSymLinkProperty(properties);
    symLinks.set(symbolicLinkName, pageTwoName);
    pageOne.commit(data);
  }

  private WikiPageProperty getSymLinkProperty(final WikiPageProperties properties) {
    WikiPageProperty symLinks = properties.getProperty(SymbolicPage.PROPERTY_NAME);
    symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    return symLinks;
  }

  private class TestRevisionControlResponder extends RevisionControlResponder {
    public TestRevisionControlResponder() {
      super(new RevisionControlOperation(RevisionControlResponderTest.this.revisionControlOperation, "", "") {

        @Override
        public void execute(final RevisionController revisionController, final String... filePath) throws RevisionControlException {
        }
      });
    }

    @Override
    protected String responseMessage(final String resource) throws Exception {
      return "End of operation.";
    }

    @Override
    protected void performOperation(final FileSystemPage page) throws Exception {
    }

  }
}
