// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.FileUtil;

public class PasswordFile {
  private File passwordFile;
  private Map<String, String> passwordMap = new HashMap<String, String>();
  private PasswordCipher cipher = new TransparentCipher();

  public PasswordFile(String filename) throws IOException {
    passwordFile = new File(filename);
    loadFile();
  }

  public PasswordFile(String filename, PasswordCipher cipher) throws IOException {
    this(filename);
    this.cipher = cipher;
  }

  public Map<String, String> getPasswordMap() {
    return passwordMap;
  }

  public String getName() {
    return passwordFile.getName();
  }

  public PasswordCipher getCipher() {
    return cipher;
  }

  public void savePassword(String user, String password) throws FileNotFoundException {
    passwordMap.put(user, cipher.encrypt(password));
    savePasswords();
  }

  private void loadFile() throws IOException {
    LinkedList<String> lines = getPasswordFileLines();
    loadCipher(lines);
    loadPasswords(lines);
  }

  private void loadPasswords(LinkedList<String> lines) {
    for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
      String line = iterator.next();
      if (!"".equals(line)) {
        String[] tokens = line.split(":");
        passwordMap.put(tokens[0], tokens[1]);
      }
    }
  }

  private void loadCipher(LinkedList<String> lines) {
    if (lines.size() > 0) {
      String firstLine = lines.getFirst().toString();
      if (firstLine.startsWith("!")) {
        String cipherClassName = firstLine.substring(1);
        try {
          instantiateCipher(cipherClassName);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        lines.removeFirst();
      }
    }
  }

  public PasswordCipher instantiateCipher(String cipherClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    cipher = (PasswordCipher) Class.forName(cipherClassName).newInstance();
    return cipher;
  }

  private void savePasswords() throws FileNotFoundException {
    List<String> lines = new LinkedList<String>();
    lines.add("!" + cipher.getClass().getName());
    for (Iterator<String> iterator = passwordMap.keySet().iterator(); iterator.hasNext();) {
      Object user = iterator.next();
      Object password = passwordMap.get(user);
      lines.add(user + ":" + password);
    }
    FileUtil.writeLinesToFile(passwordFile, lines);
  }

  private LinkedList<String> getPasswordFileLines() throws IOException {
    LinkedList<String> lines = new LinkedList<String>();
    if (passwordFile.exists())
      lines = FileUtil.getFileLines(passwordFile);
    return lines;
  }
}
