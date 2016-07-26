// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class VersionInfoTest {

  private Date toDate(String s) {
    return new Date(Long.parseLong(s) * 1000);
  }

  @Test
  public void testSortVersions() {
    List<VersionInfo> list = new LinkedList<>();
    VersionInfo toms = new VersionInfo("Tom-45678901234567", "Tom", toDate("45678901234567"));
    VersionInfo anons = new VersionInfo("56789012345678", "", toDate("56789012345678"));
    VersionInfo jerrys = new VersionInfo("Jerry-01234567890123", "Jerry", toDate("01234567890123"));
    VersionInfo joes = new VersionInfo("Joe-43210987654321", "Joe", toDate("43210987654321"));
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

  @Test
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
