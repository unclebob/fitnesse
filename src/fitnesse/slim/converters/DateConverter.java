// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter implements Converter {
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

  public String toString(Object o) {
    return dateFormat.format((Date) o);
  }

  public Object fromString(String arg) {
    try {
      return dateFormat.parse(arg);
    } catch (ParseException e) {
      throw new SlimError("Can't parse date " + arg, e);
    }
  }
}
