// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

public class TransparentCipher implements PasswordCipher {
  public String encrypt(String password) throws Exception {
    return password;
  }
}
