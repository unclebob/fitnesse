// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import fitnesse.responders.versions.VersionSelectionResponder;

import junit.framework.TestCase;

public class VersionInfoTest extends TestCase {
  public void testRegex() throws Exception {
    Matcher match = VersionInfo.COMPEX_NAME_PATTERN.matcher("01234567890123");
    assertTrue(match.find());
    assertEquals("01234567890123", match.group(2));

    match = VersionInfo.COMPEX_NAME_PATTERN.matcher("Joe-01234567890123");
    assertTrue(match.find());
    assertEquals("Joe", match.group(1));
    assertEquals("01234567890123", match.group(2));
  }

  public void testGetVersionNumber() throws Exception {
    assertEquals("01234567890123", VersionInfo.getVersionNumber("01234567890123"));
    assertEquals("01234567890123", VersionInfo.getVersionNumber("Joe-01234567890123"));
    assertEquals("09876543210987", VersionInfo.getVersionNumber("Joe-09876543210987"));
  }

  public void testSortVersions() throws Exception {
    List<VersionInfo> list = new LinkedList<VersionInfo>();
    VersionInfo toms = new VersionInfo("Tom-45678901234567");
    VersionInfo anons = new VersionInfo("56789012345678");
    VersionInfo jerrys = new VersionInfo("Jerry-01234567890123");
    VersionInfo joes = new VersionInfo("Joe-43210987654321");
    list.add(toms);
    list.add(anons);
    list.add(jerrys);
    list.add(joes);

    Collections.sort(list);

    assertEquals(jerrys, list.get(0));
    assertEquals(joes, list.get(1));
    assertEquals(toms, list.get(2));
    assertEquals(anons, list.get(3));
  }

  public void testParts() throws Exception {
    VersionInfo version = new VersionInfo("joe-20030101010101");
    assertEquals("joe", version.getAuthor());
    assertEquals("joe-20030101010101", version.getName());
  }

  public void testGetCreationTime() throws Exception {
    VersionInfo version = new VersionInfo("joe-20030101010101");
    Date date = version.getCreationTime();
    assertEquals("20030101010101", VersionInfo.makeVersionTimeFormat().format(date));
  }

  public void testGetAuthor() throws Exception {
    checkAuthor("01234567890123", "");
    checkAuthor("123-01234567890123", "");
    checkAuthor("-123-01234567890123", "");
    checkAuthor("user-01234567890123", "user");
    checkAuthor("user-123-01234567890123", "user");
    checkAuthor("abc123-123-01234567890123", "abc123");
    checkAuthor("abc123efg-123-01234567890123", "abc123efg");
    checkAuthor("joe <joe@blo.com>-123-01234567890123", "joe <joe@blo.com>");
  }

  private void checkAuthor(String versionName, String author) throws Exception {
    VersionInfo version = new VersionInfo(versionName);
    assertEquals(author, version.getAuthor());
  }

  public void testConvertVersionNameToAge() throws Exception {
    Date now = new GregorianCalendar(2003, 0, 1, 00, 00, 01).getTime();
    Date tenSeconds = new GregorianCalendar(2003, 0, 1, 00, 00, 11).getTime();
    Date twoMinutes = new GregorianCalendar(2003, 0, 1, 00, 02, 01).getTime();
    Date fiftyNineSecs = new GregorianCalendar(2003, 0, 1, 00, 01, 00).getTime();
    Date oneHour = new GregorianCalendar(2003, 0, 1, 01, 00, 01).getTime();
    Date fiveDays = new GregorianCalendar(2003, 0, 6, 00, 00, 01).getTime();
    Date years = new GregorianCalendar(2024, 0, 1, 00, 00, 01).getTime();

    assertEquals("10 seconds", VersionInfo.howLongAgoString(now, tenSeconds));
    assertEquals("2 minutes", VersionInfo.howLongAgoString(now, twoMinutes));
    assertEquals("59 seconds", VersionInfo.howLongAgoString(now, fiftyNineSecs));
    assertEquals("1 hour", VersionInfo.howLongAgoString(now, oneHour));
    assertEquals("5 days", VersionInfo.howLongAgoString(now, fiveDays));
    assertEquals("21 years", VersionInfo.howLongAgoString(now, years));
  }


}
