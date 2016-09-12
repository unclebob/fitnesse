// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class SerializedPageResponderTest {
  private FitNesseContext context;
  private WikiPage root;
  private MockRequest request;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
  }

  @After
  public void tearDown() throws Exception {
    String rootPath = "TestRooT";
    FileUtil.deleteFileSystemDirectory(rootPath);
  }

  private Object doSetUpWith(FitNesseContext context, String proxyType) throws Exception {
    WikiPage page1 = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("PageOne"), "this is page one");
    PageData data = page1.getData();
    data.setAttribute("Attr1", "true");
    page1.commit(data);
    WikiPageUtil.addPage(page1, PathParser.parse("ChildOne"), "this is child one");

    request.addInput("type", proxyType);
    request.setResource("PageOne");

    return getObject(context, request);
  }

  private Object getObject(FitNesseContext context, MockRequest request) throws Exception {
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
    return ois.readObject();
  }

  @Test
  public void testGetContentAndAttributes() throws Exception {
    Object obj = doSetUpWith(context, "meat");
    assertNotNull(obj);
    assertTrue(obj instanceof PageData);
    PageData data = (PageData) obj;

    assertEquals("this is page one", data.getContent());

    WikiPageProperty props = data.getProperties();
    assertTrue(props.has("Attr1"));
  }

  @Test
  public void testGetVersionOfPageData() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "some content");
    VersionInfo commitRecord = page.commit(page.getData());

    request.addInput("type", "meat");
    request.addInput("version", commitRecord.getName());
    request.setResource("PageOne");

    Object obj = getObject(context, request);
    assertEquals(PageData.class, obj.getClass());
    PageData data = (PageData) obj;
    assertEquals("some content", data.getContent());
  }

  @Test
  public void testGetPageHieratchyAsXml() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildOne"), "");
    WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "");

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
  }

  @Test
  public void testGetPageHieratchyAsXmlDoesntContainSymbolicLinks() throws Exception {
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.ChildOne"), "");
    WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "");

    PageData data = pageOne.getData();
    WikiPageProperty properties = data.getProperties();
    WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    symLinks.set("SymPage", "PageTwo");
    pageOne.commit(data);

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
    assertNotSubString("SymPage", xml);
  }

  @Test
  public void testGetDataAsHtml() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TestPageOne"), "test page");

    request.setResource("TestPageOne");
    request.addInput("type", "data");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("test page", xml);
    assertSubString("<Test", xml);
  }
}
