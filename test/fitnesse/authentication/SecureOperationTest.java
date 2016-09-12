// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.*;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

public class SecureOperationTest {
  private SecureReadOperation sro;
  private WikiPage root;
  FitNesseContext context;
  private MockRequest request;
  private WikiPagePath parentPagePath;
  private WikiPagePath childPagePath;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    sro = new SecureReadOperation();
    request = new MockRequest();
    parentPagePath = PathParser.parse("ParentPage");
    childPagePath = PathParser.parse("ChildPage");
  }

  @Test
  public void testNormalPageDoesNotRequireAuthentication() throws Exception {
    String insecurePageName = "InsecurePage";
    WikiPagePath insecurePagePath = PathParser.parse(insecurePageName);
    WikiPageUtil.addPage(root, insecurePagePath);
    request.setResource(insecurePageName);
    assertFalse(sro.shouldAuthenticate(context, request));
  }

  @Test
  public void testReadSecurePageRequresAuthentication() throws Exception {
    String securePageName = "SecurePage";
    WikiPagePath securePagePath = PathParser.parse(securePageName);
    WikiPage securePage = WikiPageUtil.addPage(root, securePagePath);
    makeSecure(securePage);
    request.setResource(securePageName);
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  private void makeSecure(WikiPage securePage) throws Exception {
    PageData data = securePage.getData();
    data.setAttribute(WikiPageProperty.SECURE_READ);
    securePage.commit(data);
  }

  @Test
  public void testChildPageOfSecurePageRequiresAuthentication() throws Exception {
    WikiPage parentPage = WikiPageUtil.addPage(root, parentPagePath);
    makeSecure(parentPage);
    WikiPageUtil.addPage(parentPage, childPagePath);
    request.setResource("ParentPage.ChildPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  @Test
  public void testNonExistentPageCanBeAuthenticated() throws Exception {
    request.setResource("NonExistentPage");
    assertFalse(sro.shouldAuthenticate(context, request));
  }

  @Test
  public void testParentOfNonExistentPageStillSetsPrivileges() throws Exception {
    WikiPage parentPage = WikiPageUtil.addPage(root, parentPagePath);
    makeSecure(parentPage);
    request.setResource("ParentPage.NonExistentPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  @Test
  public void testChildPageIsRestricted() throws Exception {
    WikiPage parentPage = WikiPageUtil.addPage(root, parentPagePath);
    WikiPage childPage = WikiPageUtil.addPage(parentPage, childPagePath);
    makeSecure(childPage);
    request.setResource("ParentPage.ChildPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  @Test
  public void testBlankResource() throws Exception {
    request.setResource("");
    assertFalse(sro.shouldAuthenticate(context, request));
  }
}
