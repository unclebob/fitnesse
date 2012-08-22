// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses Slim Serialization.  See ListSerializer for details.  Will deserialize lists of lists recursively.
 */

public class ListDeserializer {
  private ArrayList<Object> result;

  public static List<Object> deserialize(String serialized) {
    return new ListDeserializer(serialized).deserialize();
  }

  private String serialized;
  private int index = 0;

  public ListDeserializer(String serialized) {
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
    else if (serialized.length() == 0)
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
    result = new ArrayList<Object>();

    int itemCount = getLength();
    for (int i = 0; i < itemCount; i++)
      deserializeItem();

    return result;
  }

  private void deserializeItem() {
    int itemLength = getLength();
    String item = getString(itemLength);
    try {
      List<Object> sublist = ListDeserializer.deserialize(item);
      result.add(sublist);
    } catch (SyntaxError e) {
      result.add(item);
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
    int lengthSize = 6;
    String lengthString = serialized.substring(index, index + lengthSize);
    int length = Integer.parseInt(lengthString);
    index += lengthSize;
    checkForColon("Length");
    return length;
  }

  public class SyntaxError extends SlimError {
    private static final long serialVersionUID = 1L;

    public SyntaxError(String s) {
      super(s);
    }

    public SyntaxError(Throwable e) {
      super(e);
    }
  }
}
