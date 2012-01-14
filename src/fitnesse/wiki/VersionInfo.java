// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Clock;

public class VersionInfo implements Comparable<VersionInfo>, Serializable {
  private static final long serialVersionUID = 1L;

  public static final Pattern COMPEX_NAME_PATTERN = Pattern.compile("(?:([a-zA-Z][^\\-]*)-)?(?:\\d+-)?(\\d{14})");
  private static int counter = 0;

  public static SimpleDateFormat makeVersionTimeFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }

  public static int nextId() {
    return counter++;
  }

  private String name;
  private String author;
  private Date creationTime;

  public VersionInfo(String name, String author, Date creationTime) {
    this.name = name;
    this.author = author;
    this.creationTime = creationTime;
  }

  public VersionInfo(String complexName) {
    this(complexName, "", Clock.currentDate());
    Matcher match = COMPEX_NAME_PATTERN.matcher(complexName);
    if (match.find()) {
      author = match.group(1);
      if (author == null)
        author = "";
      try {
        creationTime = makeVersionTimeFormat().parse(match.group(2));
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public String getAuthor() {
    return author;
  }

  public Date getCreationTime() {
    return creationTime;
  }

  public String getName() {
    return name;
  }

  public String getAge() {
    Date now = new GregorianCalendar().getTime();
    return howLongAgoString(now, getCreationTime());
  }
  
  public static String getVersionNumber(String complexName) {
    Matcher match = COMPEX_NAME_PATTERN.matcher(complexName);
    match.find();
    return match.group(2);
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

  public int compareTo(VersionInfo o) {
    VersionInfo otherVersion;
    if (o instanceof VersionInfo) {
      otherVersion = ((VersionInfo) o);
      return getCreationTime().compareTo(otherVersion.getCreationTime());
    } else
      return 0;
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
