// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package util;

import junit.framework.TestCase;

public class GracefulNamerTest extends TestCase {
  public void testIsGracefulName() throws Exception {
    assertTrue(GracefulNamer.isGracefulName("My Nice Fixture"));
    assertTrue(GracefulNamer.isGracefulName("My_Nice Fixture"));
    assertTrue(GracefulNamer.isGracefulName("My-Nice-Fixture"));
    assertTrue(GracefulNamer.isGracefulName("My!Really#Crazy--Name^"));
    assertTrue(GracefulNamer.isGracefulName("EndsWithADot."));
    assertFalse(GracefulNamer.isGracefulName("MyNiceFixture"));
    assertFalse(GracefulNamer.isGracefulName("my.package.Fixture"));
  }

  public void testUnGracefulName() throws Exception {
    assertEquals("BadCompany", GracefulNamer.disgrace("Bad Company"));
    assertEquals("BadCompany", GracefulNamer.disgrace("bad company"));
    assertEquals("BadCompany", GracefulNamer.disgrace("Bad-Company"));
    assertEquals("BadCompany", GracefulNamer.disgrace("Bad Company."));
    assertEquals("BadCompany", GracefulNamer.disgrace("(Bad Company)"));
    assertEquals("BadCompany", GracefulNamer.disgrace("BadCompany"));
    assertEquals("Bad123Company", GracefulNamer.disgrace("bad 123 company"));
    assertEquals("Bad123Company", GracefulNamer.disgrace("bad 123company"));
    assertEquals("Bad123Company", GracefulNamer.disgrace("   bad  \t123  company   "));
    assertEquals("Bad123Company", GracefulNamer.disgrace("Bad123Company"));

    // Just to let you know... probably not what you want.
    assertEquals("MyNamespaceBad123Company", GracefulNamer.disgrace("My.Namespace.Bad123Company"));
  }

  public void testRegracingName() throws Exception {
    assertEquals("Company", GracefulNamer.regrace("Company"));
    assertEquals("Bad Company", GracefulNamer.regrace("BadCompany"));
    assertEquals("Bad Company Two", GracefulNamer.regrace("BadCompanyTwo"));
    assertEquals("Bad Company 123", GracefulNamer.regrace("BadCompany123"));
    assertEquals("Bad 123 Company", GracefulNamer.regrace("Bad123Company"));
    assertEquals("Bad 1a 2b 3 Company", GracefulNamer.regrace("Bad1a2b3Company"));
    assertEquals("B 12z 3 Company", GracefulNamer.regrace("B12z3Company"));
    assertEquals(".Bad Company Two", GracefulNamer.regrace(".BadCompanyTwo"));
    assertEquals(">Bad Company Two", GracefulNamer.regrace(">BadCompanyTwo"));
    assertEquals("<Bad Company Two", GracefulNamer.regrace("<BadCompanyTwo"));
    assertEquals(".Bad Company Two .Child Page", GracefulNamer.regrace(".BadCompanyTwo.ChildPage"));
    assertEquals(">Bad Company Two .Child Page", GracefulNamer.regrace(">BadCompanyTwo.ChildPage"));
    assertEquals("<Bad Company Two .Child Page", GracefulNamer.regrace("<BadCompanyTwo.ChildPage"));
  }

  public void testEmptyString() throws Exception {
    assertEquals("", GracefulNamer.disgrace(""));
  }


}
