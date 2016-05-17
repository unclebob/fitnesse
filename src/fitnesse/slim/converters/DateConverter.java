// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;
import fitnesse.util.StringUtils;

public class DateConverter implements Converter<Date> {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

  @Override
  public String toString(Date o) {
    return o != null ? DATE_FORMAT.format(o) : NULL_VALUE;
  }

  @Override
  public Date fromString(String arg) {
    if (StringUtils.isBlank(arg))
      return null;
    try {
      return DATE_FORMAT.parse(arg);
    } catch (ParseException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to date.>>", arg), e);
    }
  }
}
