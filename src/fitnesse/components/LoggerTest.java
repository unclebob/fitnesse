// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;
import util.FileUtil;

public class LoggerTest extends TestCase {
  private final String dirPath = "testLogs";
  private Logger l;
  private LogData ld;
  private String filename = "fitnesse20030306134205.log";
  private String logLine = "myHost - - [06/Mar/2003:13:42:05 -0100] \"request\" 42 666";
  private Locale saveLocale;

  public void setUp() throws Exception {
    saveLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    l = new Logger(dirPath);
    ld = new LogData();
    ld.host = "myHost";
    ld.requestLine = "request";
    ld.size = 666;
    ld.status = 42;
    TimeZone z = TimeZone.getTimeZone("GMT-1:00");
    ld.time = new GregorianCalendar(2003, 2, 6, 13, 42, 5);
    ld.time.setTimeZone(z);
  }

  public void tearDown() throws Exception {
    l.close();
    FileUtil.deleteFileSystemDirectory(dirPath);
    Locale.setDefault(saveLocale);
  }

  public void testTimeZoneHandling() {
    // let's figure out how this stuff works...
    Calendar calendar = new GregorianCalendar(2003, 0, 2, 3, 4, 5);
    calendar.setTimeZone(TimeZone.getTimeZone("GMT+2"));

    DateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss Z");

    DateFormat format2 = (DateFormat) format.clone();
    format2.setTimeZone(calendar.getTimeZone());

    assertEquals("Jan 02, 2003 03:04:05 +0200", format2.format(calendar.getTime()));
  }

  public void testConstruction() throws Exception {
    assertEquals(dirPath, l.getDirectory().getName());
    File dir = new File(dirPath);
    assertEquals(true, dir.exists());
    assertEquals(true, dir.isDirectory());
  }

  public void testLogFormat() throws Exception {
    String line = l.formatLogLine(ld);
    assertEquals(logLine, line);
  }

  public void testLogFileName() throws Exception {
    String logName = Logger.makeLogFileName(ld.time);
    assertEquals(filename, logName);
  }

  public void testLoggingOneLineInNewFile() throws Exception {
    l.log(ld);
    l.close();
    File dir = l.getDirectory();
    File file = new File(dir, filename);
    assertTrue(file.exists());
    String contents = FileUtil.getFileContent(file);
    assertEquals(logLine + System.getProperty("line.separator"), contents);
  }

  public void testLogSecondLineInSameFile() throws Exception {
    l.log(ld);
    LogData ld2 = (LogData) ld.clone();
    ld2.host = "newHost";
    l.log(ld2);
    File dir = l.getDirectory();
    File file = new File(dir, filename);
    BufferedReader br = new BufferedReader(new FileReader(file));
    assertEquals(logLine, br.readLine());
    assertEquals("newHost - - [06/Mar/2003:13:42:05 -0100] \"request\" 42 666", br.readLine());
    assertTrue(br.readLine() == null);
    br.close();
  }

  public void testLogLineInNewFile() throws Exception {
    LogData nextDay = (LogData) ld.clone();
    nextDay.time.add(Calendar.DATE, 1);
    l.log(ld);
    l.log(nextDay);
    l.close();
    File firstFile = getLogFileFor(ld);
    File secondFile = getLogFileFor(nextDay);
    assertTrue(firstFile.exists());
    assertTrue(secondFile.exists());
    String firstContent = FileUtil.getFileContent(firstFile);
    assertEquals(l.formatLogLine(ld) + System.getProperty("line.separator"), firstContent);
    String secondContent = FileUtil.getFileContent(secondFile);
    assertEquals(l.formatLogLine(nextDay) + System.getProperty("line.separator"), secondContent);
  }

  public void testLoggingIncludesUsername() throws Exception {
    ld.username = "Joe";
    l.log(ld);
    l.close();
    File dir = l.getDirectory();
    File file = new File(dir, filename);
    assertTrue(file.exists());
    String contents = FileUtil.getFileContent(file);
    logLine = "myHost - Joe [06/Mar/2003:13:42:05 -0100] \"request\" 42 666";
    assertEquals(logLine + System.getProperty("line.separator"), contents);
  }

  private File getLogFileFor(LogData data) {
    return new File(l.getDirectory(), Logger.makeLogFileName(data.time));
  }
}
