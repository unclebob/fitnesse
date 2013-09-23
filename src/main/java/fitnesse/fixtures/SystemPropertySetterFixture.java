// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class SystemPropertySetterFixture extends ColumnFixture {
  public String key;
  public String value;

  public void execute() {
    System.getProperties().setProperty(key, value);
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
