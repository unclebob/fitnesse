// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static fitnesse.wiki.PageData.PropertyLAST_MODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageUtil;

import org.junit.Before;
import org.junit.Test;

public class SavePropertiesResponderTest {
  private static final String PAGE_NAME = "PageOne";
  private FitNesseContext context;
  private WikiPage root;
  private MockRequest request;
  private WikiPage page;
  private Responder responder;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    responder = new SavePropertiesResponder();
  }

  private void createRequest() throws Exception {
    page = WikiPageUtil.addPage(root, PathParser.parse(PAGE_NAME), "");

    request = new MockRequest();
    request.addInput("PageType", "Test");
    request.addInput("Properties", "on");
    request.addInput("Search", "on");
    request.addInput("RecentChanges", "on");
    request.addInput(PageData.PropertyPRUNE,"on");
    request.addInput(PageData.PropertySECURE_READ, "on");
    request.addInput("Suites", "Suite A, Suite B");
    request.addInput("HelpText", "Help text literal");
    request.setResource(PAGE_NAME);
  }

  @Test
  public void testResponse() throws Exception {
    createRequest();

    Response response = responder.makeResponse(context, request);

    PageData data = page.getData();
    assertTrue(data.hasAttribute("Test"));
    assertTrue(data.hasAttribute("Properties"));
    assertTrue(data.hasAttribute("Search"));
    assertFalse(data.hasAttribute("Edit"));
    assertTrue(data.hasAttribute("RecentChanges"));
    assertTrue(data.hasAttribute(PageData.PropertySECURE_READ));
    assertFalse(data.hasAttribute(PageData.PropertySECURE_WRITE));
    assertTrue(data.hasAttribute(PageData.PropertyPRUNE));
    assertEquals("Suite A, Suite B", data.getAttribute(PageData.PropertySUITES));
    assertEquals("Help text literal", data.getAttribute(PageData.PropertyHELP));

    assertEquals(303, response.getStatus());
    assertEquals("/" + PAGE_NAME, response.getHeader("Location"));
  }
  @Test
  public void testRemovesHelpAndSuitesAttributeIfEmpty() throws Exception {
    createRequest();
    request.addInput("Suites", "");
    request.addInput("HelpText", "");
    
    responder.makeResponse(context, request);
    
    PageData data = page.getData();
    assertFalse("should not have help attribute", data.hasAttribute(PageData.PropertyHELP));
    assertFalse("should not have suites attribute", data.hasAttribute(PageData.PropertySUITES));
  }

  @Test
  public void testPageDataDefaultAttributesAreKeptWhenSavedThroughSavePropertiesResponder() throws Exception {
    // The old way the default attributes were set in PageData.initializeAttributes()
    // was to set them with a value of "true"
    // The SavePropertiesResponder saves them by setting the attribute without a value.
    // This test ensures that the behavior is the same (i.e. without value)
    page = WikiPageUtil.addPage(root, PathParser.parse(PAGE_NAME), "");
    PageData defaultData = page.getData();

    request = new MockRequest();
    request.setResource(PAGE_NAME);
    setBooleanAttributesOnRequest(defaultData, PageData.NON_SECURITY_ATTRIBUTES);
    setBooleanAttributesOnRequest(defaultData, PageData.SECURITY_ATTRIBUTES);

    responder.makeResponse(context, request);

    PageData dataToSave = page.getData();
    // The LasModified Attribute is the only one that might be different, so fix it here
    dataToSave.setAttribute(PropertyLAST_MODIFIED, defaultData.getAttribute(PropertyLAST_MODIFIED));
    WikiPageProperties defaultWikiPagePropertiesDefault = new WikiPageProperties(defaultData.getProperties());
    WikiPageProperties wikiPagePropertiesToSave = new WikiPageProperties(dataToSave.getProperties());
    assertEquals(defaultWikiPagePropertiesDefault.toXml(), wikiPagePropertiesToSave.toXml());
  }

  private void setBooleanAttributesOnRequest(PageData data,
      String[] booleanAttributes) {
    for (String booleanAttribute : booleanAttributes) {
      if (data.hasAttribute(booleanAttribute)) {
        request.addInput(booleanAttribute, "on");
      }
    }
  }
}
