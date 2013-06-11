// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.wiki.*;
import junit.framework.TestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;

public class SecureOperationTest extends TestCase {
  private SecureReadOperation sro;
  private WikiPage root;
  FitNesseContext context;
  private MockRequest request;
  private PageBuilder pageBuilder;
  private WikiPagePath parentPagePath;
  private WikiPagePath childPagePath;

  protected void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    sro = new SecureReadOperation();
    request = new MockRequest();
    pageBuilder = new PageBuilder();
    parentPagePath = PathParser.parse("ParentPage");
    childPagePath = PathParser.parse("ChildPage");
  }

  public void testNormalPageDoesNotRequireAuthentication() throws Exception {
    String insecurePageName = "InsecurePage";
    WikiPagePath insecurePagePath = PathParser.parse(insecurePageName);
    pageBuilder.addPage(root, insecurePagePath);
    request.setResource(insecurePageName);
    assertFalse(sro.shouldAuthenticate(context, request));
  }

  public void testReadSecurePageRequresAuthentication() throws Exception {
    String securePageName = "SecurePage";
    WikiPagePath securePagePath = PathParser.parse(securePageName);
    WikiPage securePage = pageBuilder.addPage(root, securePagePath);
    makeSecure(securePage);
    request.setResource(securePageName);
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  private void makeSecure(WikiPage securePage) throws Exception {
    PageData data = securePage.getData();
    data.setAttribute(PageData.PropertySECURE_READ);
    securePage.commit(data);
  }

  public void testChildPageOfSecurePageRequiresAuthentication() throws Exception {
    WikiPage parentPage = pageBuilder.addPage(root, parentPagePath);
    makeSecure(parentPage);
    pageBuilder.addPage(parentPage, childPagePath);
    request.setResource("ParentPage.ChildPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  public void testNonExistentPageCanBeAuthenticated() throws Exception {
    request.setResource("NonExistentPage");
    assertFalse(sro.shouldAuthenticate(context, request));
  }

  public void testParentOfNonExistentPageStillSetsPrivileges() throws Exception {
    WikiPage parentPage = pageBuilder.addPage(root, parentPagePath);
    makeSecure(parentPage);
    request.setResource("ParentPage.NonExistentPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  public void testChildPageIsRestricted() throws Exception {
    WikiPage parentPage = pageBuilder.addPage(root, parentPagePath);
    WikiPage childPage = pageBuilder.addPage(parentPage, childPagePath);
    makeSecure(childPage);
    request.setResource("ParentPage.ChildPage");
    assertTrue(sro.shouldAuthenticate(context, request));
  }

  public void testBlankResource() throws Exception {
    request.setResource("");
    assertFalse(sro.shouldAuthenticate(context, request));
  }
}
