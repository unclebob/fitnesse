// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import java.util.HashMap;
import java.util.Map;

public class MultiUserAuthenticator extends Authenticator {
  private Map<String, String> users = new HashMap<String, String>();
  private PasswordCipher cipher;

  public MultiUserAuthenticator(String passwdFile) throws Exception {
    PasswordFile passwords = new PasswordFile(passwdFile);
    users = passwords.getPasswordMap();
    cipher = passwords.getCipher();
  }

  public boolean isAuthenticated(String username, String password) throws Exception {
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
