// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.protocol;

import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.SlimVersion;

/**
 * Uses Slim Serialization.  See SlimSerializer for details.  Will deserialize lists of lists recursively.
 */

public class SlimDeserializer {
  private List<Object> result;

  public static List<Object> deserialize(String serialized) {
    return new SlimDeserializer(serialized).deserialize();
  }

  private String serialized;
  private int index = 0;

  public SlimDeserializer(String serialized) {
    this.serialized = serialized;
  }


  public List<Object> deserialize() {
    try {
      checkSerializedStringIsValid();
      return deserializeString();
    } catch (Exception e) {
      throw new SyntaxError(e);
    }
  }

  private void checkSerializedStringIsValid() {
    if (serialized == null)
      throw new SyntaxError("Can't deserialize null");
    else if (serialized.isEmpty())
      throw new SyntaxError("Can't deserialize empty string");
  }

  private List<Object> deserializeString() {
    checkForOpenBracket();
    List<Object> result = deserializeList();
    checkForClosedBracket();
    return result;
  }

  private void checkForClosedBracket() {
    if (!charsLeft() || getChar() != ']')
      throw new SyntaxError("Serialized list has no ending ]");
  }

  private boolean charsLeft() {
    return index < serialized.length();
  }

  private void checkForOpenBracket() {
    if (getChar() != '[')
      throw new SyntaxError("Serialized list has no starting [");
  }

  private List<Object> deserializeList() {
    result = new ArrayList<>();

    int itemCount = getLength();
    for (int i = 0; i < itemCount; i++)
      deserializeItem();

    return result;
  }

  private void deserializeItem() {
    int itemLength = getLength();
    String item = getString(itemLength);
    List<Object> sublist = maybeReadList(item);

    if (sublist == null)
      result.add(item);
    else
      result.add(sublist);
  }

  /**
   * @return the string parsed as a list if possible, null otherwise
   */
  private List<Object> maybeReadList(String string) {
    if ("".equals(string.trim()) || !string.startsWith("["))
      return null;

    try {
      return SlimDeserializer.deserialize(string);
    } catch (SyntaxError e) {
      return null;
    }
  }

  private String getString(int length) {
    String result = serialized.substring(index, index + length);
    index += length;
    checkForColon("String");
    return result;
  }

  private void checkForColon(String itemType) {
    if (getChar() != ':')
      throw new SyntaxError(itemType + " in serialized list not terminated by colon.");

  }

  private char getChar() {
    return serialized.charAt(index++);
  }

  private int getLength() {
    try {
      return tryGetLength();
    } catch (NumberFormatException e) {
      throw new SyntaxError(e);
    }
  }

  private int tryGetLength() {
    int lengthSize = SlimVersion.MINIMUM_NUMBER_LENGTH;
    String lengthString = serialized.substring(index, index + lengthSize);
    int length = Integer.parseInt(lengthString);
    index += lengthSize;

    Integer next;
    while ((next = maybeReadDigit()) != null)
      length = length * 10 + next;

    checkForColon("Length");
    return length;
  }

  private Integer maybeReadDigit() {
    char next = serialized.charAt(index);
    if (Character.isDigit(next)) {
      index++;
      return Character.digit(next, 10);
    } else
      return null;
  }

}
