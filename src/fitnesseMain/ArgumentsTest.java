// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import junit.framework.TestCase;
import fitnesse.Arguments;

public class ArgumentsTest extends TestCase {
  private Arguments args;

  public void testSimpleCommandline() throws Exception {
    args = makeArgs(new String[0]);
    assertNotNull(args);
    assertEquals(80, args.getPort());
    assertEquals(".", args.getRootPath());
  }

  private Arguments makeArgs(String[] argArray) {
    return args = FitNesseMain.parseCommandLine(argArray);
  }

  public void testArgumentsDefaults() throws Exception {
    makeArgs(new String[]{});
    assertEquals(80, args.getPort());
    assertEquals(".", args.getRootPath());
    assertEquals("FitNesseRoot", args.getRootDirectory());
    assertEquals(null, args.getLogDirectory());
    assertEquals(false, args.isOmittingUpdates());
    assertEquals(14, args.getDaysTillVersionsExpire());
    assertEquals(null, args.getUserpass());
  }

  public void testArgumentsAlternates() throws Exception {
    String argString = "-p 123 -d MyWd -r MyRoot -l LogDir -e 321 -o -a userpass.txt";
    makeArgs(argString.split(" "));
    assertEquals(123, args.getPort());
    assertEquals("MyWd", args.getRootPath());
    assertEquals("MyRoot", args.getRootDirectory());
    assertEquals("LogDir", args.getLogDirectory());
    assertEquals(true, args.isOmittingUpdates());
    assertEquals(321, args.getDaysTillVersionsExpire());
    assertEquals("userpass.txt", args.getUserpass());
  }

  public void testAllArguments() throws Exception {
    args = makeArgs(new String[]{"-p", "81", "-d", "directory", "-r", "root", "-l", "myLogDirectory",
      "-o", "-e", "22"});
    assertNotNull(args);
    assertEquals(81, args.getPort());
    assertEquals("directory", args.getRootPath());
    assertEquals("root", args.getRootDirectory());
    assertEquals("myLogDirectory", args.getLogDirectory());
    assertTrue(args.isOmittingUpdates());
    assertEquals(22, args.getDaysTillVersionsExpire());
  }

  public void testNotOmitUpdates() throws Exception {
    args = makeArgs(new String[]{"-p", "81", "-d", "directory", "-r", "root", "-l", "myLogDirectory"});
    assertNotNull(args);
    assertEquals(81, args.getPort());
    assertEquals("directory", args.getRootPath());
    assertEquals("root", args.getRootDirectory());
    assertEquals("myLogDirectory", args.getLogDirectory());
    assertTrue(!args.isOmittingUpdates());

  }

  public void testBadArgument() throws Exception {
    args = makeArgs(new String[]{"-x"});
    assertNull(args);
  }
}
