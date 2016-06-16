// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.FileUtil;

public class PasswordFile {
  private final File passwordFile;
  private Map<String, String> passwordMap = new HashMap<>();
  private PasswordCipher cipher = new TransparentCipher();

  public PasswordFile(String filename) throws IOException, ReflectiveOperationException {
    passwordFile = new File(filename);
    loadFile();
  }

  public PasswordFile(String filename, PasswordCipher cipher) throws IOException, ReflectiveOperationException {
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

  private void loadFile() throws IOException, ReflectiveOperationException {
    LinkedList<String> lines = getPasswordFileLines();
    loadCipher(lines);
    loadPasswords(lines);
  }

  private void loadPasswords(LinkedList<String> lines) {
    for (String line : lines) {
      if (!"".equals(line)) {
        String[] tokens = line.split(":");
        passwordMap.put(tokens[0], tokens[1]);
      }
    }
  }

  private void loadCipher(LinkedList<String> lines) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
    if (!lines.isEmpty()) {
      String firstLine = lines.getFirst();
      if (firstLine.startsWith("!")) {
        String cipherClassName = firstLine.substring(1);
        instantiateCipher(cipherClassName);
        lines.removeFirst();
      }
    }
  }

  public PasswordCipher instantiateCipher(String cipherClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    cipher = (PasswordCipher) Class.forName(cipherClassName).newInstance();
    return cipher;
  }

  private void savePasswords() throws FileNotFoundException {
    List<String> lines = new LinkedList<>();
    lines.add("!" + cipher.getClass().getName());
    for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
      String user = entry.getKey();
      String password = entry.getValue();
      lines.add(user + ":" + password);
    }
    FileUtil.writeLinesToFile(passwordFile, lines);
  }

  private LinkedList<String> getPasswordFileLines() throws IOException {
    LinkedList<String> lines = new LinkedList<>();
    if (passwordFile.exists())
      lines = FileUtil.getFileLines(passwordFile);
    return lines;
  }
}
