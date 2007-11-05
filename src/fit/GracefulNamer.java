// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit;

import java.util.regex.*;
import fitnesse.wiki.PathParser;

public class GracefulNamer
{
  private static Pattern disgracefulNamePattern = Pattern
      .compile("\\w(?:[.]|\\w)*[^.]");

  public static boolean isGracefulName(String fixtureName)
  {
    Matcher matcher = disgracefulNamePattern.matcher(fixtureName);
    return !matcher.matches();
  }

  public static String disgrace(String fixtureName)
  {
    GracefulNamer namer = new GracefulNamer();

    for (int i = 0; i < fixtureName.length(); i++)
    {
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

  public static String regrace(String disgracefulName)
  {
    final char separator = PathParser.PATH_SEPARATOR.charAt(0);
    char c = '?';
    GracefulNamer namer = new GracefulNamer();
    if (disgracefulName.length() > 0)
       namer.finalName.append(c = disgracefulName.charAt(0));

    boolean isGrabbingDigits = false, wasSeparator = (PathParser.isPathPrefix(c));
    for (int i = 1; i < disgracefulName.length(); i++)
    {  c = disgracefulName.charAt(i);
       if (   (Character.isUpperCase(c))
           || (Character.isDigit(c) && !isGrabbingDigits)
           || (c == separator)
          )
       {
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

  private GracefulNamer()
  {
  }

  private interface GracefulNameState
  {
    public void letter(char c);

    public void digit(char c);

    public void other(char c);
  }

  private class InWordState implements GracefulNameState
  {
    public void letter(char c)
    {
      finalName.append(c);
    }

    public void digit(char c)
    {
      finalName.append(c);
      currentState = new InNumberState();
    }

    public void other(char c)
    {
      currentState = new OutOfWordState();
    }
  }

  private class InNumberState implements GracefulNameState
  {
    public void letter(char c)
    {
      finalName.append(Character.toUpperCase(c));
      currentState = new InWordState();
    }

    public void digit(char c)
    {
      finalName.append(c);
    }

    public void other(char c)
    {
      currentState = new OutOfWordState();
    }
  }

  private class OutOfWordState implements GracefulNameState
  {
    public void letter(char c)
    {
      finalName.append(Character.toUpperCase(c));
      currentState = new InWordState();
    }

    public void digit(char c)
    {
      finalName.append(c);
      currentState = new InNumberState();
    }

    public void other(char c)
    {
    }
  }
}