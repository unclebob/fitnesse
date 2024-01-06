// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Password {
  public static final String defaultFile = "passwords.txt";
  public static final String defaultCipher = "fitnesse.authentication.HashingCipher";

  private static BufferedReader input;

  private PasswordFile passwords;
  private String username;
  private String password;
  private PasswordCipher cipher;

  public static void main(String[] args) throws Exception {
    Password password = new Password();
    if (!password.args(args))
      printUsage();

    input = new BufferedReader(new InputStreamReader(System.in));
    password.interactForPassword();

    password.savePassword();
    System.out.println("password saved in " + password.passwords.getName());
  }

  public boolean doesUserExist(String name) {
    return passwords.getPasswordMap().get(name) != null;
  }

  public void deletePassword(String username) throws Exception {
    passwords.deleteUser(username);
  }

  public static void printUsage() {
    System.err.println("Usage: java fitnesse.authentication.Password [-f <password file>] [-c <password cipher>] <user>");
    System.err.println("\t-f <password file> {" + defaultFile + "}");
    System.err.println("\t-c <password cipher> {" + defaultCipher + "}");
    System.exit(-1);
  }

  public Password(String filename) throws Exception {
    cipher = new HashingCipher();
    passwords = new PasswordFile(filename, cipher);
  }

  public Password() throws Exception {
    this(defaultFile);
  }

  public void savePassword(String usernamePassed, String passwordPassed) throws Exception {
    passwords.savePassword(usernamePassed, passwordPassed);
  }

  private void savePassword() throws Exception {
    passwords.savePassword(username, password);
  }

  public boolean args(String[] args) {
    if (args.length < 1 || args.length > 5)
      return false;

    try {
      boolean done = false;
      int argIndex = 0;
      while (!done) {
        if (args[argIndex].startsWith("-")) {
          if ("-f".equals(args[argIndex])) {
            passwords = new PasswordFile(args[argIndex + 1], cipher);
            argIndex += 2;
          } else if ("-c".equals(args[argIndex])) {
            cipher = passwords.instantiateCipher(args[argIndex + 1]);
            argIndex += 2;
          } else
            return false;
        } else {
          username = args[argIndex];
          done = true;
        }
      }
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public String getUsername() {
    return username;
  }

  public String getFilename() {
    return passwords.getName();
  }

  public PasswordCipher getCipher() {
    return cipher;
  }

  private void interactForPassword() throws Exception {
    while (password == null) {
      System.out.println("Be advised, the password will be visible as it is typed.");
      System.out.print("enter password for " + username + ": ");
      String password1 = getUserEntry();
      System.out.print("confirm password: ");
      String password2 = getUserEntry();

      if (password1 != null && password1.equals(password2))
        password = password1;
      else {
        System.out.println("");
        System.out.println("passwords did not match");
      }
    }
  }

  private String getUserEntry() throws Exception {
    return input.readLine();
  }
}
