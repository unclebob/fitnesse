// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.Arguments;
import static org.junit.Assert.*;
import org.junit.Test;

public class ArgumentsTest {
  private Arguments args;

  @Test
  public void testSimpleCommandline() throws Exception {
    args = makeArgs(new String[0]);
    assertNotNull(args);
    assertEquals(80, args.getPort());
    assertEquals(".", args.getRootPath());
  }

  private Arguments makeArgs(String... argArray) {
    return args = FitNesseMain.parseCommandLine(argArray);
  }

  @Test
  public void testArgumentsDefaults() throws Exception {
    makeArgs();
    assertEquals(80, args.getPort());
    assertEquals(".", args.getRootPath());
    assertEquals("FitNesseRoot", args.getRootDirectory());
    assertEquals(null, args.getLogDirectory());
    assertEquals(false, args.isOmittingUpdates());
    assertEquals(14, args.getDaysTillVersionsExpire());
    assertEquals(null, args.getUserpass());
    assertEquals(false, args.isInstallOnly());
    assertNull(args.getCommand());
  }

  @Test
  public void testArgumentsAlternates() throws Exception {
    String argString = "-p 123 -d MyWd -r MyRoot -l LogDir -e 321 -o -a userpass.txt -i";
    makeArgs(argString.split(" "));
    assertEquals(123, args.getPort());
    assertEquals("MyWd", args.getRootPath());
    assertEquals("MyRoot", args.getRootDirectory());
    assertEquals("LogDir", args.getLogDirectory());
    assertEquals(true, args.isOmittingUpdates());
    assertEquals(321, args.getDaysTillVersionsExpire());
    assertEquals("userpass.txt", args.getUserpass());
    assertEquals(true, args.isInstallOnly());
  }

  @Test
  public void testAllArguments() throws Exception {
    args = makeArgs("-p", "81", "-d", "directory", "-r", "root",
        "-l", "myLogDirectory", "-o", "-e", "22");
    assertNotNull(args);
    assertEquals(81, args.getPort());
    assertEquals("directory", args.getRootPath());
    assertEquals("root", args.getRootDirectory());
    assertEquals("myLogDirectory", args.getLogDirectory());
    assertTrue(args.isOmittingUpdates());
    assertEquals(22, args.getDaysTillVersionsExpire());
  }

  @Test
  public void testNotOmitUpdates() throws Exception {
    args = makeArgs("-p", "81", "-d", "directory", "-r", "root",
        "-l", "myLogDirectory");
    assertNotNull(args);
    assertEquals(81, args.getPort());
    assertEquals("directory", args.getRootPath());
    assertEquals("root", args.getRootDirectory());
    assertEquals("myLogDirectory", args.getLogDirectory());
    assertFalse(args.isOmittingUpdates());
  }

  @Test
  public void commandShouldUseDifferentDefaultPort() throws Exception {
    args = makeArgs("-c", "someCommand");
    assertNotNull(args);
    assertEquals(Arguments.DEFAULT_COMMAND_PORT, args.getPort());
  }

  @Test
  public void commandShouldAllowPortToBeSet() throws Exception {
    args = makeArgs("-c", "someCommand", "-p", "666");
    assertNotNull(args);
    assertEquals(666, args.getPort());
  }

  @Test
  public void testBadArgument() throws Exception {
    args = makeArgs("-x");
    assertNull(args);
  }
}
