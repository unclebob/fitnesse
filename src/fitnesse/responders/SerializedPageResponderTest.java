// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import fitnesse.wiki.*;
import util.FileUtil;
import util.RegexTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;

public class SerializedPageResponderTest extends RegexTestCase {
  private final String RootPath = "TestRooT";
  private WikiPage root;
  private MockRequest request;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    request = new MockRequest();
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(RootPath);
  }

  private Object doSetUpWith(WikiPage root, String proxyType) throws Exception {
    WikiPage page1 = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "this is page one");
    PageData data = page1.getData();
    data.setAttribute("Attr1", "true");
    page1.commit(data);
    WikiPageUtil.addPage(page1, PathParser.parse("ChildOne"), "this is child one");

    request.addInput("type", proxyType);
    request.setResource("PageOne");

    return getObject(root, request);
  }

  private Object getObject(WikiPage root, MockRequest request) throws Exception {
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);

    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
    return ois.readObject();
  }

  public void testGetContentAndAttributes() throws Exception {
    Object obj = doSetUpWith(root, "meat");
    assertNotNull(obj);
    assertTrue(obj instanceof PageData);
    PageData data = (PageData) obj;

    assertEquals("this is page one", data.getContent());

    WikiPageProperties props = data.getProperties();
    assertTrue(props.has("Attr1"));
  }

  public void testGetVersionOfPageData() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "some content");
    VersionInfo commitRecord = page.commit(page.getData());

    request.addInput("type", "meat");
    request.addInput("version", commitRecord.getName());
    request.setResource("PageOne");

    Object obj = getObject(root, request);
    assertEquals(PageData.class, obj.getClass());
    PageData data = (PageData) obj;
    assertEquals("some content", data.getContent());
  }

  public void testGetPageHieratchyAsXml() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"));
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildOne"));
    WikiPageUtil.addPage(root, PathParser.parse("PageTwo"));

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
  }

  public void testGetPageHieratchyAsXmlDoesntContainSymbolicLinks() throws Exception {
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"));
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildOne"));
    WikiPageUtil.addPage(root, PathParser.parse("PageTwo"));

    PageData data = pageOne.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    symLinks.set("SymPage", "PageTwo");
    pageOne.commit(data);

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
    assertNotSubString("SymPage", xml);
  }

  public void testGetDataAsHtml() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TestPageOne"), "test page");

    request.setResource("TestPageOne");
    request.addInput("type", "data");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("test page", xml);
    assertSubString("<Test", xml);
  }
}
