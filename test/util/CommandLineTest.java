// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CommandLineTest {
  private CommandLine options;

  @Test
  public void testSimpleParsing() throws Exception {
    assertTrue(createOptionsAndParse("", ""));
    assertFalse(createOptionsAndParse("", "blah"));
  }

  @Test
  public void testOneRequiredArgument() throws Exception {
    assertFalse(createOptionsAndParse("arg1", ""));

    assertTrue(createOptionsAndParse("arg1", "blah"));
    assertEquals("blah", options.getArgument("arg1"));
  }

  @Test
  public void testThreeRequiredArguments() throws Exception {
    assertTrue(createOptionsAndParse("arg1 arg2 arg3", "tic tac toe"));
    assertEquals("tic", options.getArgument("arg1"));
    assertEquals("tac", options.getArgument("arg2"));
    assertEquals("toe", options.getArgument("arg3"));
  }

  @Test
  public void testOneSimpleOption() throws Exception {
    assertTrue(createOptionsAndParse("[-opt1]", ""));
    assertFalse(options.hasOption("opt1"));
    assertTrue(createOptionsAndParse("[-opt1]", "-opt1"));
    assertTrue(options.hasOption("opt1"));
  }

  @Test
  public void testOptionWithArgument() throws Exception {
    assertFalse(createOptionsAndParse("[-opt1 arg]", "-opt1"));

    assertTrue(createOptionsAndParse("[-opt1 arg]", "-opt1 blah"));
    assertTrue(options.hasOption("opt1"));
    String argument = options.getOptionArgument("opt1", "arg");
    assertNotNull(argument);
    assertEquals("blah", argument);
  }

  @Test
  public void testMultipleOptions(){
    options = new CommandLine("[-opt1 arg]");
    String[] args = new Option().split("-opt1 blah");
    assertTrue(options.parse(args));
    options.hasOption("opt1");
    assertEquals("blah", options.getOptionArgument("opt1", "arg"));
  }

  @Test
  public void testInvalidOption() throws Exception {
    assertFalse(createOptionsAndParse("", "-badArg"));
  }

  @Test
  public void testCombo() throws Exception {
    String descriptor = "[-opt1 arg1 arg2] [-opt2 arg1] [-opt3] arg1 arg2";

    assertFalse(createOptionsAndParse(descriptor, ""));
    assertFalse(createOptionsAndParse(descriptor, "a"));
    assertFalse(createOptionsAndParse(descriptor, "-opt1 a b c"));
    assertFalse(createOptionsAndParse(descriptor, "-opt2 a b"));
    assertFalse(createOptionsAndParse(descriptor, "-opt2 -opt3 a b"));
    assertFalse(createOptionsAndParse(descriptor, "-opt1 a -opt2 b -opt3 c d"));
    assertFalse(createOptionsAndParse(descriptor, "-opt1 a b -opt2 c -opt3 d e f"));

    assertTrue(createOptionsAndParse(descriptor, "a b"));
    assertTrue(createOptionsAndParse(descriptor, "-opt3 a b"));
    assertTrue(createOptionsAndParse(descriptor, "-opt2 a b c"));
    assertTrue(createOptionsAndParse(descriptor, "-opt1 a b c d"));
    assertTrue(createOptionsAndParse(descriptor, "-opt1 a b -opt2 c d e"));

    assertTrue(createOptionsAndParse(descriptor, "-opt1 a b -opt2 c -opt3 d e"));
    assertTrue(options.hasOption("opt1"));
    assertEquals("a", options.getOptionArgument("opt1", "arg1"));
    assertEquals("b", options.getOptionArgument("opt1", "arg2"));
    assertTrue(options.hasOption("opt2"));
    assertEquals("c", options.getOptionArgument("opt2", "arg1"));
    assertTrue(options.hasOption("opt3"));
    assertEquals("d", options.getArgument("arg1"));
    assertEquals("e", options.getArgument("arg2"));
  }

  private boolean createOptionsAndParse(String validOptions, String enteredOptions) {
    options = new CommandLine(validOptions);
    String[] args = new Option().split(enteredOptions);
    return options.parse(args);
  }
}
