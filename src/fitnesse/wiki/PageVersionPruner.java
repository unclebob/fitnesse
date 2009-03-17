// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class PageVersionPruner {
  public static int daysTillVersionsExpire = 14;

  public static void pruneVersions(FileSystemPage page, Collection<VersionInfo> versions) throws Exception {
    List<VersionInfo> versionsList = makeSortedVersionList(versions);
    if (versions.size() > 0) {
      VersionInfo lastVersion = versionsList.get(versionsList.size() - 1);
      GregorianCalendar expirationDate = makeVersionExpirationDate(lastVersion);
      for (Iterator<VersionInfo> iterator = versionsList.iterator(); iterator.hasNext();) {
        VersionInfo version = iterator.next();
        removeVersionIfExpired(page, version, expirationDate);
      }
    }
  }

  private static List<VersionInfo> makeSortedVersionList(Collection<VersionInfo> versions) throws Exception {
    List<VersionInfo> versionsList = new ArrayList<VersionInfo>(versions);
    Collections.sort(versionsList);
    return versionsList;
  }

  private static GregorianCalendar makeVersionExpirationDate(VersionInfo lastVersion) throws Exception {
    Date dateOfLastVersion = lastVersion.getCreationTime();
    GregorianCalendar expirationDate = new GregorianCalendar();
    expirationDate.setTime(dateOfLastVersion);
    expirationDate.add(Calendar.DAY_OF_MONTH, -(daysTillVersionsExpire));
    return expirationDate;
  }

  private static void removeVersionIfExpired(FileSystemPage page, VersionInfo version, GregorianCalendar expirationDate) throws Exception {
    Calendar thisDate = new GregorianCalendar();
    thisDate.setTime(version.getCreationTime());
    if (thisDate.before(expirationDate) || thisDate.equals(expirationDate))
      page.removeVersion(version.getName());
  }
}
