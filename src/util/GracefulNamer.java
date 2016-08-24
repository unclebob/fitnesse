// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GracefulNamer {
  private static final Pattern disgracefulNamePattern = Pattern
    .compile("\\w(?:[.]|\\w)*[^.]");

  public static boolean isGracefulName(String fixtureName) {
    Matcher matcher = disgracefulNamePattern.matcher(fixtureName);
    return !matcher.matches();
  }

  public static String disgrace(String fixtureName) {
    GracefulNamer namer = new GracefulNamer();

    for (int i = 0; i < fixtureName.length(); i++) {
      char c = fixtureName.charAt(i);
      if (Character.isLetter(c))
        namer.currentState.letter(c);
      else if (Character.isDigit(c))
        namer.currentState.digit(c);
      else
        namer.currentState.other(c);
    }

    return namer.finalName.toString();
  }

  public static String regrace(String disgracefulName) {
    final char separator = '.';
    char c = '?';
    GracefulNamer namer = new GracefulNamer();
    if (!disgracefulName.isEmpty())
      namer.finalName.append(c = disgracefulName.charAt(0));

    boolean isGrabbingDigits = false;
    boolean wasSeparator = c == '.' || c == '<' || c == '>';
    for (int i = 1; i < disgracefulName.length(); i++) {
      c = disgracefulName.charAt(i);
      if ((Character.isUpperCase(c))
        || (Character.isDigit(c) && !isGrabbingDigits)
        || (c == separator)
        ) {
        if (!wasSeparator) namer.finalName.append(" ");
        wasSeparator = (c == separator);
      }

      isGrabbingDigits = (Character.isDigit(c));
      namer.finalName.append(c);
    }

    return namer.finalName.toString();
  }

  private StringBuffer finalName = new StringBuffer();

  private GracefulNameState currentState = new OutOfWordState();

  private GracefulNamer() {
  }

  private interface GracefulNameState {
    void letter(char c);

    void digit(char c);

    void other(char c);
  }

  private class InWordState implements GracefulNameState {
    @Override
    public void letter(char c) {
      finalName.append(c);
    }

    @Override
    public void digit(char c) {
      finalName.append(c);
      currentState = new InNumberState();
    }

    @Override
    public void other(char c) {
      currentState = new OutOfWordState();
    }
  }

  private class InNumberState implements GracefulNameState {
    @Override
    public void letter(char c) {
      finalName.append(Character.toUpperCase(c));
      currentState = new InWordState();
    }

    @Override
    public void digit(char c) {
      finalName.append(c);
    }

    @Override
    public void other(char c) {
      currentState = new OutOfWordState();
    }
  }

  private class OutOfWordState implements GracefulNameState {
    @Override
    public void letter(char c) {
      finalName.append(Character.toUpperCase(c));
      currentState = new InWordState();
    }

    @Override
    public void digit(char c) {
      finalName.append(c);
      currentState = new InNumberState();
    }

    @Override
    public void other(char c) {
    }
  }
}
