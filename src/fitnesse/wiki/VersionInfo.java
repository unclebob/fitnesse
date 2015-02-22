// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Date;
import java.util.GregorianCalendar;

public class VersionInfo implements Comparable<VersionInfo> {
  private String name;
  private String author;
  private Date creationTime;

  public VersionInfo(String name, String author, Date creationTime) {
    this.name = name;
    this.author = author;
    this.creationTime = new Date(creationTime.getTime());
  }

  public static VersionInfo makeVersionInfo(String author, Date creationTime) {
    String versionName = WikiImportProperty.getTimeFormat().format(creationTime);
    if (author != null && !"".equals(author)) {
      versionName = author + "-" + versionName;
    }
    return new VersionInfo(versionName, author, creationTime);
  }

  public static VersionInfo makeVersionInfo(final PageData data) {
    return makeVersionInfo(data.getAttribute(PageData.LAST_MODIFYING_USER),
            data.getProperties().getLastModificationTime());
  }

  public String getAuthor() {
    return author;
  }

  public Date getCreationTime() {
    return new Date(creationTime.getTime());
  }

  public String getName() {
    return name;
  }

  public String getAge() {
    Date now = new GregorianCalendar().getTime();
    return howLongAgoString(now, getCreationTime());
  }
  
  public static String howLongAgoString(Date now, Date then) {
    long time = Math.abs(now.getTime() - then.getTime()) / 1000;

    if (time < 60)
      return pluralize(time, "second");
    else if (time < 3600)
      return pluralize(time / 60, "minute");
    else if (time < 86400)
      return pluralize(time / (3600), "hour");
    else if (time < 31536000)
      return pluralize(time / (86400), "day");
    else
      return pluralize(time / (31536000), "year");
  }

  private static String pluralize(long time, String unit) {
    String age = time + " " + unit;
    if (time > 1)
      age = age + "s";

    return age;
  }

  public int compareTo(VersionInfo otherVersion) {
      return getCreationTime().compareTo(otherVersion.getCreationTime());
  }

  public String toString() {
    return getName();
  }

  public boolean equals(Object o) {
    if (o != null && o instanceof VersionInfo) {
      VersionInfo otherVersion = (VersionInfo) o;
      return getName().equals(otherVersion.getName());
    } else
      return false;
  }

  public int hashCode() {
    return getName().hashCode();
  }
}
