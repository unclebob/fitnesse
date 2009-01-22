// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

/**
 * Slim is all strings at the interface.  So we need converters that will map strings to types and types back to
 * strings.  Each derivative of this interface corresponds to a particular type.
 */
public interface Converter {
  public String toString(Object o);

  Object fromString(String arg);
}
