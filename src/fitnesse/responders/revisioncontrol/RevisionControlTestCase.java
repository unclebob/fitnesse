// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.VERSIONED;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static util.RegexTestCase.assertSubString;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;
import util.FileUtil;
import util.StandardOutAndErrorRecorder;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;

public abstract class RevisionControlTestCase extends TestCase {
  protected static final String FS_PARENT_PAGE = "ExternalParent";
  protected static final String FS_CHILD_PAGE = "ExternalChild";
  protected static final String FS_GRAND_CHILD_PAGE = "ExternalGrandChild";
  protected static final String ROOT = "testDir";
  protected static final String FS_SIBLING_CHILD_PAGE = "ExternalChildTwo";
  protected FitNesseContext context;
  protected MockRequest request;
  protected SimpleResponse response;
  protected Responder responder;
  protected WikiPage root;
  protected FileSystemPage parentPage;
  protected FileSystemPage childPage;
  protected FileSystemPage grandChildPage;
  protected RevisionController revisionController = createMock(RevisionController.class);
  private StandardOutAndErrorRecorder standardOutAndErrorRecorder;


  @Override
  protected void setUp() throws Exception {
    standardOutAndErrorRecorder = new StandardOutAndErrorRecorder();

    reset(this.revisionController);
    createExternalRoot();
    this.request = new MockRequest();
    expect(this.revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
    this.revisionController.add((String) anyObject());
    expectLastCall().anyTimes();
    expect(this.revisionController.checkState(rootContentAndPropertiesFilePath())).andStubReturn(VERSIONED);
  }

  @Override
  protected void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(ROOT);
    verify(this.revisionController);
    standardOutAndErrorRecorder.stopRecording(false);
  }

  protected void createPage(final String pageName) throws Exception {
    final PageCrawler crawler = this.root.getPageCrawler();
    if (FS_PARENT_PAGE.equals(pageName)) {
      this.parentPage = (FileSystemPage) crawler.addPage(this.root, PathParser.parse(FS_PARENT_PAGE));
    }
    if (FS_CHILD_PAGE.equals(pageName)) {
      createPage(FS_PARENT_PAGE);
      this.childPage = (FileSystemPage) crawler.addPage(this.parentPage, PathParser.parse(FS_CHILD_PAGE));
    }
    if (FS_GRAND_CHILD_PAGE.equals(pageName)) {
      createPage(FS_CHILD_PAGE);
      this.grandChildPage = (FileSystemPage) crawler.addPage(this.childPage, PathParser.parse(FS_GRAND_CHILD_PAGE));
    }
  }

  protected FileSystemPage createPage(final String pageName, final FileSystemPage parent) throws Exception {
    final PageCrawler crawler = this.root.getPageCrawler();
    return (FileSystemPage) crawler.addPage(parent, PathParser.parse(pageName));
  }

  protected void invokeResponderAndCheckSuccessStatus() throws Exception {
    invokeResponderAndCheckStatusIs(200);
  }

  protected void invokeResponderAndCheckStatusIs(final int status) throws Exception {
    invokeResponderAndGetResponse();
    assertEquals(status, this.response.getStatus());
  }

  protected void createExternalRoot() throws Exception {
    FileUtil.createDir(ROOT);
    this.root = FileSystemPage.makeRoot(ROOT, "ExternalRoot", this.revisionController);
    this.context = new FitNesseContext(this.root);
  }

  protected void invokeResponderAndCheckResponseContains(final String responseMessage) throws Exception {
    invokeResponderAndGetResponse();
    assertSubString(responseMessage, this.response.getContent());
  }

  private void invokeResponderAndGetResponse() throws Exception {
    this.response = (SimpleResponse) this.responder.makeResponse(this.context, this.request);
  }

  protected String[] contentAndPropertiesFilePathFor(final String page) throws Exception {
    final String pageName = folderPath(page);
    return new String[]{contentFilePathFor(pageName), propertiesFilePathFor(pageName)};
  }

  private String folderPath(final String page) {
    return ROOT + "/ExternalRoot" + fullPageName(page, "/");
  }

  private String fullPageName(final String pageName, final String delimiter) {
    String fullPageName = "";
    if (pageName != null) {
      if (FS_PARENT_PAGE.equals(pageName)) {
        fullPageName += delimiter + FS_PARENT_PAGE;
      } else if (FS_CHILD_PAGE.equals(pageName)) {
        fullPageName += delimiter + FS_PARENT_PAGE + delimiter + FS_CHILD_PAGE;
      } else if (FS_SIBLING_CHILD_PAGE.equals(pageName)) {
        fullPageName += delimiter + FS_PARENT_PAGE + delimiter + FS_SIBLING_CHILD_PAGE;
      } else if (FS_GRAND_CHILD_PAGE.equals(pageName)) {
        fullPageName += delimiter + FS_PARENT_PAGE + delimiter + FS_CHILD_PAGE + delimiter + FS_GRAND_CHILD_PAGE;
      }
    }
    return fullPageName;
  }

  protected void assertPageDoesNotExists(final String pageName) throws Exception {
    assertFalse(pageName + " still exists!", this.root.getPageCrawler().pageExists(this.root, PathParser.parse(fullPageName(pageName, "."))));
  }

  protected String rootFolderFilePath() {
    return new File(folderPath(null)).getAbsolutePath();
  }

  protected String folderFilePath(final String pageName) {
    return new File(folderPath(pageName)).getAbsolutePath();
  }

  protected String[] rootContentAndPropertiesFilePath() throws Exception {
    return contentAndPropertiesFilePathFor(null);
  }

  private String propertiesFilePathFor(final String page) throws Exception {
    return fileSystemPath(page, FileSystemPage.propertiesFilename);
  }

  private String contentFilePathFor(final String page) throws Exception {
    return fileSystemPath(page, FileSystemPage.contentFilename);
  }

  private String fileSystemPath(final String page, final String fileName) throws Exception {
    return new File((page + fileName).replace('/', File.separatorChar)).getAbsolutePath();
  }
}
