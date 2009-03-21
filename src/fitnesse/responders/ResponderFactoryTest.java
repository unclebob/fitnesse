// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.revisioncontrol.RevisionControlOperation.SYNC;
import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.responders.editing.SavePropertiesResponder;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.responders.editing.SymbolicLinkResponder;
import fitnesse.responders.files.CreateDirectoryResponder;
import fitnesse.responders.files.DeleteConfirmationResponder;
import fitnesse.responders.files.DeleteFileResponder;
import fitnesse.responders.files.FileResponder;
import fitnesse.responders.files.RenameFileConfirmationResponder;
import fitnesse.responders.files.RenameFileResponder;
import fitnesse.responders.files.UploadResponder;
import fitnesse.responders.refactoring.DeletePageResponder;
import fitnesse.responders.refactoring.MovePageResponder;
import fitnesse.responders.refactoring.RefactorPageResponder;
import fitnesse.responders.refactoring.RenamePageResponder;
import fitnesse.responders.revisioncontrol.AddResponder;
import fitnesse.responders.revisioncontrol.CheckinResponder;
import fitnesse.responders.revisioncontrol.CheckoutResponder;
import fitnesse.responders.revisioncontrol.DeleteResponder;
import fitnesse.responders.revisioncontrol.RevertResponder;
import fitnesse.responders.revisioncontrol.SyncResponder;
import fitnesse.responders.revisioncontrol.UpdateResponder;
import fitnesse.responders.run.*;
import fitnesse.responders.search.ExecuteSearchPropertiesResponder;
import fitnesse.responders.search.SearchFormResponder;
import fitnesse.responders.search.SearchResponder;
import fitnesse.responders.search.WhereUsedResponder;
import fitnesse.responders.versions.RollbackResponder;
import fitnesse.responders.versions.VersionResponder;
import fitnesse.responders.versions.VersionSelectionResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class ResponderFactoryTest extends TestCase {
  private ResponderFactory factory;
  private MockRequest request;
  private WikiPageDummy nonExistantPage;
  private WikiPage root;
  private PageCrawler crawler;

  @Override
  public void setUp() throws Exception {
    factory = new ResponderFactory("testDir");
    request = new MockRequest();
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    nonExistantPage = new WikiPageDummy();
  }

  public void testGetResponderKey() throws Exception {
    checkResponderKey("railroad", "railroad");
    checkResponderKey("responder=railroad", "railroad");
    checkResponderKey("", "");
  }

  private void checkResponderKey(String queryString, String key) {
    MockRequest request = new MockRequest();
    request.setQueryString(queryString);
    assertEquals(key, factory.getResponderKey(request));
  }

  public void testWikiPageResponder() throws Exception {
    request.setResource("SomePage");
    assertResponderType(WikiPageResponder.class, root);
    request.setResource("");
    assertResponderType(WikiPageResponder.class, root);
    request.setResource("root");
    assertResponderType(WikiPageResponder.class, root);
  }

  public void testRefactorPageResponder() throws Exception {
    assertResponderTypeMatchesInput("refactor", RefactorPageResponder.class);
  }

  public void testDeletePageResponder() throws Exception {
    assertResponderTypeMatchesInput("deletePage", DeletePageResponder.class);
  }

  public void testRenamePageResponder() throws Exception {
    assertResponderTypeMatchesInput("renamePage", RenamePageResponder.class);
  }

  public void testEditResponder() throws Exception {
    request.addInput("responder", "edit");
    request.setResource("SomePage");
    assertResponderType(EditResponder.class, root);
    assertResponderType(EditResponder.class, nonExistantPage);
  }

  public void testDontCreatePageResponder() throws Exception {
    request.addInput("responder", "dontCreatePage");
    request.setResource("SomePage");
    assertResponderType(NotFoundResponder.class, root);
    assertResponderType(NotFoundResponder.class, nonExistantPage);
  }

  public void testPageDataResponder() throws Exception {
    request.addInput("responder", "pageData");
    request.setResource("SomePage");
    assertResponderType(PageDataWikiPageResponder.class, root);
  }

  public void testSaveResponder() throws Exception {
    assertResponderTypeMatchesInput("saveData", SaveResponder.class);
  }

  public void testTestResponder() throws Exception {
    assertResponderTypeMatchesInput("test", TestResponder.class);
  }

  public void testSuiteResponder() throws Exception {
    assertResponderTypeMatchesInput("suite", SuiteResponder.class);
  }

  public void testFileResponder() throws Exception {
    try {
      new File("testDir").mkdir();
      new File("testDir/files").mkdir();
      FileUtil.createFile("testDir/files/someFile", "this is a test");
      request.setResource("files/someFile");
      assertResponderType(FileResponder.class, nonExistantPage);
    } finally {
      FileUtil.deleteFileSystemDirectory("testDir");
    }
  }

  public void testSearchFormResponder() throws Exception {
    assertResponderTypeMatchesInput("searchForm", SearchFormResponder.class);
  }

  public void testSearchResponder() throws Exception {
    assertResponderTypeMatchesInput("search", SearchResponder.class);
  }

  public void testSerializedPageResponder() throws Exception {
    assertResponderTypeMatchesInput("proxy", SerializedPageResponder.class);
  }

  public void testVersionSelectionResponder() throws Exception {
    assertResponderTypeMatchesInput("versions", VersionSelectionResponder.class);
  }

  public void testVersionResponder() throws Exception {
    assertResponderTypeMatchesInput("viewVersion", VersionResponder.class);
  }

  public void testRollbackResponder() throws Exception {
    assertResponderTypeMatchesInput("rollback", RollbackResponder.class);
  }

  public void testNameReponder() throws Exception {
    assertResponderTypeMatchesInput("names", NameWikiPageResponder.class);
  }

  public void testUploadResponder() throws Exception {
    assertResponderTypeMatchesInput("upload", UploadResponder.class);
  }

  public void testCreateDirectoryResponder() throws Exception {
    assertResponderTypeMatchesInput("createDir", CreateDirectoryResponder.class);
  }

  public void testDeleteFileResponder() throws Exception {
    assertResponderTypeMatchesInput("deleteFile", DeleteFileResponder.class);
  }

  public void testRenameFileResponder() throws Exception {
    assertResponderTypeMatchesInput("renameFile", RenameFileResponder.class);
  }

  public void testDeleteConfirmationFileResponder() throws Exception {
    assertResponderTypeMatchesInput("deleteConfirmation", DeleteConfirmationResponder.class);
  }

  public void testRenameFileConfirmationResponder() throws Exception {
    assertResponderTypeMatchesInput("renameConfirmation", RenameFileConfirmationResponder.class);
  }

  public void testCreatePropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("properties", PropertiesResponder.class);
  }

  public void testCreateSavePropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("saveProperties", SavePropertiesResponder.class);
  }

  public void testCreateExecuteSearchPropertiesResponder() throws Exception {
    assertResponderTypeMatchesInput("executeSearchProperties", ExecuteSearchPropertiesResponder.class);
  }

  public void testCreateWhereUsedResponder() throws Exception {
    assertResponderTypeMatchesInput("whereUsed", WhereUsedResponder.class);
  }

  public void testCreateMovePageResponer() throws Exception {
    assertResponderTypeMatchesInput("movePage", MovePageResponder.class);
  }

  public void testSocketCatcher() throws Exception {
    assertResponderTypeMatchesInput("socketCatcher", SocketCatchingResponder.class);
  }

  public void testFitClient() throws Exception {
    assertResponderTypeMatchesInput("fitClient", FitClientResponder.class);
  }

  public void testRawContent() throws Exception {
    assertResponderTypeMatchesInput("raw", RawContentResponder.class);
  }

  public void testRssResponder() throws Exception {
    assertResponderTypeMatchesInput("rss", RssResponder.class);
  }

  public void testPageImporterResponder() throws Exception {
    assertResponderTypeMatchesInput("import", WikiImportingResponder.class);
  }

  public void testShutdownResponder() throws Exception {
    assertResponderTypeMatchesInput("shutdown", ShutdownResponder.class);
  }

  public void testTestResultFormattingResponder() throws Exception {
    assertResponderTypeMatchesInput("format", TestResultFormattingResponder.class);
  }

  public void testSymbolicLinkResponder() throws Exception {
    assertResponderTypeMatchesInput("symlink", SymbolicLinkResponder.class);
  }

  public void testPacketResponder() throws Exception {
    assertResponderTypeMatchesInput("packet", PacketResponder.class);
  }

  public void testStopTestResponder() throws Exception {
    assertResponderTypeMatchesInput("stoptest", StopTestResponder.class);
  }
  
  public void testWillDisplayVirtualPages() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"));
    crawler.addPage(page1, PathParser.parse("ChildOne"), "child content");
    WikiPage page2 = crawler.addPage(root, PathParser.parse("PageTwo"));
    FitNesseUtil.bindVirtualLinkToPage(page2, page1);
    request.setResource("PageTwo.ChildOne");
    assertResponderType(WikiPageResponder.class, root);
  }

  public void testNotFoundResponder() throws Exception {
    request.setResource("somepage");
    assertResponderType(NotFoundResponder.class, root);
  }

  public void testAddingResponders() throws Exception {
    factory.addResponder("custom", WikiPageResponder.class);
    assertResponderTypeMatchesInput("custom", WikiPageResponder.class);
  }

  public void testAddToRevisionControlResponder() throws Exception {
    assertResponderTypeMatchesInput(ADD.getQuery(), AddResponder.class);
  }

  public void testSyncronizeRevisionControlResponder() throws Exception {
    assertResponderTypeMatchesInput(SYNC.getQuery(), SyncResponder.class);
  }

  public void testCheckoutResponder() throws Exception {
    assertResponderTypeMatchesInput(CHECKOUT.getQuery(), CheckoutResponder.class);
  }

  public void testCheckinResponder() throws Exception {
    assertResponderTypeMatchesInput(CHECKIN.getQuery(), CheckinResponder.class);
  }

  public void testDeleteFromSourceResponder() throws Exception {
    assertResponderTypeMatchesInput(DELETE.getQuery(), DeleteResponder.class);
  }

  public void testRevertSourceControlChangesResponder() throws Exception {
    assertResponderTypeMatchesInput(REVERT.getQuery(), RevertResponder.class);
  }

  public void testUpdateSourceFromRevisionControlResponder() throws Exception {
    assertResponderTypeMatchesInput(UPDATE.getQuery(), UpdateResponder.class);
  }

  private void assertResponderType(Class<?> expectedClass, WikiPage page) throws Exception {
    Responder responder = factory.makeResponder(request, page);
    assertEquals(expectedClass, responder.getClass());
  }

  private void assertResponderTypeMatchesInput(String responderType, Class<?> responderClass) throws Exception {
    request.addInput("responder", responderType);
    assertResponderType(responderClass, root);
  }
}
