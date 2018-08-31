// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiUserAuthenticator extends Authenticator {
  private Map<String, String> users;
  private PasswordCipher cipher;

  public MultiUserAuthenticator(String passwdFile) throws IOException, ReflectiveOperationException {
    PasswordFile passwords = new PasswordFile(passwdFile);
    users = passwords.getPasswordMap();
    cipher = passwords.getCipher();
  }

  @Override
  public boolean isAuthenticated(String username, String password) {
    if (username == null || password == null)
      return false;

    String foundPassword = users.get(username);
    if (foundPassword == null)
      return false;

    String encryptedPassword = cipher.encrypt(password);
    return encryptedPassword.equals(foundPassword);
  }

  public int userCount() {
    return users.size();
  }

  public String getPasswd(String user) {
    return users.get(user);
  }
}
