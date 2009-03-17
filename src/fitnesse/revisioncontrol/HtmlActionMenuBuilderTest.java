// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.WikiPageResponder;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;

public class HtmlActionMenuBuilderTest extends TestCase

{
  private static final String REVISION_CONTROL_HEADER = "<div class=\"main\">Revision Control</div>";
  private static final String ROOT = "testDir";
  private WikiPage root;
  private final RevisionController revisionController = createMock(RevisionController.class);

  @Override
  protected void setUp() throws Exception {
    FileUtil.createDir(ROOT);
    expect(revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
    expect(revisionController.makeVersion((FileSystemPage) anyObject(), (PageData) anyObject())).andStubReturn(new VersionInfo("PageName"));
    expect(revisionController.checkState((String) anyObject())).andStubReturn(VERSIONED);
    revisionController.add((String) anyObject());
    expectLastCall().anyTimes();
    revisionController.prune((FileSystemPage) anyObject());
    expectLastCall().asStub();
  }

  @Override
  protected void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(ROOT);
    verify(revisionController);
  }

  private SimpleResponse requestPage(String name) throws Exception {
    final MockRequest request = new MockRequest();
    request.setResource(name);
    final Responder responder = new WikiPageResponder();
    return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
  }

  public void testShouldNotDisplayAnyReversionControlButtonsIfWikiIsNotUnderRevisionControl() throws Exception {
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(false);
    replay(revisionController);

    final String pageName = "EditablePage";
    createRoot();
    root.addChildPage(pageName);
    final String html = requestPage(pageName).getContent();
    assertSubString(link("Edit", pageName, "edit", "e"), html);
    assertRevisionControlItemsNotDisplayed(pageName, html);
  }

  public void testShouldNotDisplayAnyReversionControlButtonsIfPageIsNotEditableNorImported() throws Exception {
    replay(revisionController);

    final String pageName = "NonEditablePage";
    createRoot();
    final WikiPage testPage = root.addChildPage(pageName);
    final PageData pageData = testPage.getData();
    pageData.removeAttribute("Edit");
    testPage.commit(pageData);

    final String html = requestPage(pageName).getContent();
    assertNotSubString(link("Edit", pageName, "edit", "e"), html);
    assertRevisionControlItemsNotDisplayed(pageName, html);
  }

  public void testShouldDisplayRevisionControlActionMenuHeaderIfWikiIsUnderRevisionControl() throws Exception {
    final String pageName = "UnderVersionControlPage";
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
    expect(revisionController.checkState(contentAndPropertiesFilePath(ROOT + "/ExternalRoot/" + pageName))).andReturn(VERSIONED);
    replay(revisionController);

    createRoot();
    root.addChildPage(pageName);
    final String html = requestPage(pageName).getContent();
    assertRevisionControlHeaderPresent(html);
  }

  public void testShouldDisplayRevisionControlButtonIfWikiPageIsImported() throws Exception {
    final String pageName = "ImportedWikiPage";
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
    expect(revisionController.checkState(contentAndPropertiesFilePath(ROOT + "/ExternalRoot/" + pageName))).andReturn(UNKNOWN);
    replay(revisionController);

    createRoot();
    final WikiPage testPage = root.addChildPage(pageName);
    final PageData pageData = testPage.getData();
    pageData.removeAttribute("Edit");
    pageData.setAttribute("WikiImport");
    testPage.commit(pageData);
    final String html = requestPage(pageName).getContent();
    assertRevisionControlHeaderPresent(html);
  }

  public void testShouldDisplayAddToRevisionControlButtonForPages() throws Exception {
    final String pageName = "NotUnderVersionControlPage";
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
    expect(revisionController.checkState(contentAndPropertiesFilePath(ROOT + "/ExternalRoot/" + pageName))).andReturn(UNKNOWN);
    replay(revisionController);

    final String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertRevisionControlHeaderPresent(html);
    assertAddToRevisionControlButtonIsVisible(pageName, html);
  }

  public void testShouldDisplayAssociatedRevisionControlButtonForPages() throws Exception {
    final String pageName = "CheckedInPage";
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
    expect(revisionController.checkState(contentAndPropertiesFilePath(ROOT + "/ExternalRoot/" + pageName))).andReturn(VERSIONED);
    replay(revisionController);

    final String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertRevisionControlHeaderPresent(html);
    assertAddToRevisionControlButtonIsNotVisible(pageName, html);
    assertCheckoutButtonIsVisible(pageName, html);
    assertUpdateButtonIsVisible(pageName, html);
    assertDeleteButtonIsVisible(pageName, html);
  }

  public void testShouldNotDisplayRevertButtonForLocalUnchangedPages() throws Exception {
    final String pageName = "UnchangedPage";
    expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
    expect(revisionController.checkState(contentAndPropertiesFilePath(ROOT + "/ExternalRoot/" + pageName))).andReturn(VERSIONED);
    replay(revisionController);

    final String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertAddToRevisionControlButtonIsNotVisible(pageName, html);
    assertUpdateButtonIsVisible(pageName, html);
    assertRevertButtonIsNotVisible(pageName, html);
  }

  private void createRoot() throws Exception {
    root = FileSystemPage.makeRoot(ROOT, "ExternalRoot", revisionController);
  }

  private String getActionsHtml(String pageName) throws Exception {
    createRoot();
    root.addChildPage(pageName);
    return requestPage(pageName).getContent();
  }

  private void assertRevisionControlItemsNotDisplayed(String pageName, String html) throws Exception {
    assertRevisionControlHeaderNotPresent(html);
    assertAddToRevisionControlButtonIsNotVisible(pageName, html);
    assertCheckinButtonIsNotVisible(pageName, html);
    assertCheckoutButtonIsNotVisible(pageName, html);
    assertUpdateButtonIsNotVisible(pageName, html);
    assertRevertButtonIsNotVisible(pageName, html);
    assertDeleteButtonIsNotVisible(pageName, html);
  }

  private String[] contentAndPropertiesFilePath(String basePath) {
    return new String[]{new File(basePath + FileSystemPage.contentFilename).getAbsolutePath(),
      new File(basePath + FileSystemPage.propertiesFilename).getAbsolutePath()};
  }

  private void assertAddToRevisionControlButtonIsVisible(String pageName, String html) throws Exception {
    assertSubString(link("Add", pageName, "addToRevisionControl", "a"), html);
  }

  private void assertAddToRevisionControlButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Add", pageName, "addToRevisionControl", "a"), html);
  }

  private void assertCheckoutButtonIsVisible(String pageName, String html) throws Exception {
    assertSubString(link("Checkout", pageName, "checkout", "c"), html);
  }

  private void assertCheckoutButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Checkout", pageName, "checkout", "c"), html);
  }

  private void assertDeleteButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Delete", pageName, "deleteFromRevisionControl", "d"), html);
  }

  private void assertDeleteButtonIsVisible(String pageName, String html) throws Exception {
    assertSubString(link("Delete", pageName, "deleteFromRevisionControl", "d"), html);
  }

  private void assertRevertButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Revert", pageName, "revert", ""), html);
  }

  private void assertUpdateButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Update", pageName, "update", "u"), html);
  }

  private void assertUpdateButtonIsVisible(String pageName, String html) throws Exception {
    assertSubString(link("Update", pageName, "update", "u"), html);
  }

  private void assertCheckinButtonIsNotVisible(String pageName, String html) throws Exception {
    assertNotSubString(link("Checkin", pageName, "checkin", "i"), html);
  }

  private void assertRevisionControlHeaderPresent(String html) {
    assertSubString(REVISION_CONTROL_HEADER, html);
  }

  private void assertRevisionControlHeaderNotPresent(String html) {
    assertNotSubString(REVISION_CONTROL_HEADER, html);
  }

  private void verifyDefaultLinks(String html, String pageName) {
    assertSubString(link("Edit", pageName, "edit", "e"), html);
    assertSubString(link("Versions", pageName, "versions", "v"), html);
    assertSubString(link("Properties", pageName, "properties", "p"), html);
    assertSubString(link("Refactor", pageName, "refactor", "r"), html);
    assertSubString(link("Where Used", pageName, "whereUsed", "w"), html);
    assertSubString("<a href=\"/files\" accesskey=\"f\">Files</a>", html);
    assertSubString("<a href=\"?searchForm\" accesskey=\"s\">Search</a>", html);
    assertSubString("<a href=\"/RecentChanges\" accesskey=\"\">Recent Changes</a>", html);
  }

  private String link(String displayText, String pageName, String actionQuery, String accessKey) {
    return "<a href=\"" + pageName + "?" + actionQuery + "\" accesskey=\"" + accessKey + "\">" + displayText + "</a>";
  }

}
