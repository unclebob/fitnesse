// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

public class CharConverter extends ConverterBase<Character> {

  @Override
  public Character getObject(String arg) {
    return arg.charAt(0);
  }
}
