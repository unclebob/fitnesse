// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

public class EchoFixture {
  private String name;

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String echo(String s) {
    return s;
  }
   
  public int echoInt(int i) {
    return i;
  }
}
