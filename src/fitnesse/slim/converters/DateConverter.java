// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.SlimError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter extends ConverterBase<Date> {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

  @Override
  public String getString(Date o) {
    return DATE_FORMAT.format(o);
  }

  @Override
  public Date getObject(String arg) {
    try {
      return DATE_FORMAT.parse(arg);
    } catch (ParseException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to date.>>", arg), e);
    }
  }
}
