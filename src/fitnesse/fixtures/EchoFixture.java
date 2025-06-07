// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.slim.SlimIgnoreAllTestsException;
import fitnesse.slim.SlimIgnoreScriptTestException;

public class EchoFixture {
  private String name;

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean nameContains(String s) {
    return name.contains(s);
  }

  public String echo(String s) {
    return s;
  }

  public int echoInt(int i) {
    return i;
  }

  public String echoAndLog(String s) {
    System.out.println(s);
    return s;
  }

  public String ignore(String s) throws SlimIgnoreScriptTestException {
    throw new SlimIgnoreScriptTestException("");
  }

  public String ignoreAll(String s) throws SlimIgnoreAllTestsException {
    throw new SlimIgnoreAllTestsException("testing ignore all");
  }


}
