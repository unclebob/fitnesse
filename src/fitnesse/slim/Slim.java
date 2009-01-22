// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.HashMap;
import java.util.Map;

public class Slim {
  static Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();

  public static void addConverter(Class<?> k, Converter converter) {
    converters.put(k, converter);
  }
}
