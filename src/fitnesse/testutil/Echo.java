// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Echo {
  public static void main(String[] args) throws Exception {
    String s;
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    while ((s = bufferedReader.readLine()) != null)
      System.out.println(s);
    System.exit(0);
  }
}
