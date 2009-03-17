// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import util.FileUtil;
import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;

public class SymbolicLinkResponderTest extends RegexTestCase {
  private WikiPage root;
  private WikiPage pageOne, pageTwo, childTwo;
  private MockRequest request;
  private Responder responder;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");          //#  root
    pageOne = root.addChildPage("PageOne");       //#    |--PageOne
    pageOne.addChildPage("ChildOne");   //#    |    `--ChildOne
    pageTwo = root.addChildPage("PageTwo");       //#    `--PageTwo
    childTwo = pageTwo.addChildPage("ChildTwo");   //#         |--ChildTwo
    pageTwo.addChildPage("ChildThree"); //#         `--ChildThree

    request = new MockRequest();
    request.setResource("PageOne");
    responder = new SymbolicLinkResponder();
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  public void testSubmitGoodForm() throws Exception {
    executeSymbolicLinkTestWith("SymLink", "PageTwo");
  }

  public void testShouldTrimSpacesOnLinkPath() throws Exception {
    executeSymbolicLinkTestWith("SymLink", "    PageTwo   ");
  }

  public void testShouldTrimSpacesOnLinkName() throws Exception {
    executeSymbolicLinkTestWith("   SymLink   ", "PageTwo");
  }

  private void executeSymbolicLinkTestWith(String linkName, String linkPath) throws Exception {
    request.addInput("linkName", linkName);
    request.addInput("linkPath", linkPath);
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkPageOneRedirectToProperties(response);

    WikiPage symLink = pageOne.getChildPage("SymLink");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testSubmitGoodFormToSiblingChild() throws Exception {
    executeSymbolicLinkTestWith("SymLink", "PageTwo.ChildTwo");
  }

  public void testSubmitGoodFormToChildSibling() throws Exception {
    request.setResource("PageTwo.ChildTwo");
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "ChildThree");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkChildTwoRedirectToProperties(response);

    WikiPage symLink = childTwo.getChildPage("SymLink");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testSubmitGoodFormToAbsolutePath() throws Exception {
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", ".PageTwo");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkPageOneRedirectToProperties(response);

    WikiPage symLink = pageOne.getChildPage("SymLink");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testSubmitGoodFormToSubChild() throws Exception {
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", ">ChildOne");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkPageOneRedirectToProperties(response);

    SymbolicPage symLink = (SymbolicPage) (pageOne.getChildPage("SymLink"));
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testSubmitGoodFormToSibling() throws Exception {
    request.addInput("linkName", "SymTwo");
    request.addInput("linkPath", "PageTwo");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkPageOneRedirectToProperties(response);

    WikiPage symLink = pageOne.getChildPage("SymTwo");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testSubmitGoodFormToBackwardRelative() throws Exception {
    request.setResource("PageTwo.ChildTwo");
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "<PageTwo.ChildThree");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkChildTwoRedirectToProperties(response);

    WikiPage symLink = childTwo.getChildPage("SymLink");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());
  }

  public void testRemoval() throws Exception {
    PageData data = pageOne.getData();
    WikiPageProperty symLinks = data.getProperties().set(SymbolicPage.PROPERTY_NAME);
    symLinks.set("SymLink", "PageTwo");
    pageOne.commit(data);
    assertNotNull(pageOne.getChildPage("SymLink"));

    request.addInput("removal", "SymLink");
    Response response = responder.makeResponse(new FitNesseContext(root), request);
    checkPageOneRedirectToProperties(response);

    assertNull(pageOne.getChildPage("SymLink"));
  }

  public void testRename() throws Exception {
    PageData data = pageOne.getData();
    WikiPageProperty symLinks = data.getProperties().set(SymbolicPage.PROPERTY_NAME);
    symLinks.set("SymLink", "PageTwo");
    pageOne.commit(data);
    assertNotNull(pageOne.getChildPage("SymLink"));

    request.addInput("rename", "SymLink");
    request.addInput("newname", "NewLink");
    Response response = responder.makeResponse(new FitNesseContext(root), request);
    checkPageOneRedirectToProperties(response);

    assertNotNull(pageOne.getChildPage("NewLink"));
  }

  public void testNoPageAtPath() throws Exception {
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "NonExistingPage");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(404, response.getStatus());
    String content = ((SimpleResponse) response).getContent();
    assertSubString("doesn't exist", content);
    assertSubString("Error Occured", content);
  }

  public void testAddFailWhenPageAlreadyHasChild() throws Exception {
    pageOne.addChildPage("SymLink");
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "PageTwo");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(412, response.getStatus());
    String content = ((SimpleResponse) response).getContent();
    assertSubString("already has a child named SymLink", content);
    assertSubString("Error Occured", content);
  }

  public void testSubmitFormForLinkToExternalRoot() throws Exception {
    FileUtil.createDir("testDir");
    FileUtil.createDir("testDir/ExternalRoot");

    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "file://testDir/ExternalRoot");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    checkPageOneRedirectToProperties(response);

    WikiPage symLink = pageOne.getChildPage("SymLink");
    assertNotNull(symLink);
    assertEquals(SymbolicPage.class, symLink.getClass());

    WikiPage realPage = ((SymbolicPage) symLink).getRealPage();
    assertEquals(FileSystemPage.class, realPage.getClass());
    assertEquals("testDir/ExternalRoot", ((FileSystemPage) realPage).getFileSystemPath());
  }

  public void testSubmitFormForLinkToExternalRootThatsMissing() throws Exception {
    request.addInput("linkName", "SymLink");
    request.addInput("linkPath", "file://testDir/ExternalRoot");
    Response response = responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(404, response.getStatus());
    String content = ((SimpleResponse) response).getContent();
    assertSubString("Cannot create link to the file system path, <b>file://testDir/ExternalRoot</b>.", content);
    assertSubString("Error Occured", content);
  }

  private void checkPageOneRedirectToProperties(Response response) {
    assertEquals(303, response.getStatus());
    assertEquals(response.getHeader("Location"), "PageOne?properties");
  }

  private void checkChildTwoRedirectToProperties(Response response) {
    assertEquals(303, response.getStatus());
    assertEquals(response.getHeader("Location"), "PageTwo.ChildTwo?properties");
  }
}
