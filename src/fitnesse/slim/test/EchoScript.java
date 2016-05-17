// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

public class EchoScript {
  public String echo(String s) {
    return s;
  }
  public String echoToStdout(String s) {
    System.out.println(s);
    return s;
  }
  public String echoToStderr(String s) {
    System.err.println(s);
    return s;
  }
}
