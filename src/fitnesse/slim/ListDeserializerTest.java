// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ListDeserializerTest {
  private List<Object> list;

  @Before
  public void setUp() {
    list = new ArrayList<Object>();
  }

  private void check() {
    String serialized = ListSerializer.serialize(list);
    List<Object> deserialized = ListDeserializer.deserialize(serialized);
    Assert.assertEquals(list, deserialized);
  }

  @Test(expected = ListDeserializer.SyntaxError.class)
  public void cantDeserializeNullString() throws Exception {
    ListDeserializer.deserialize(null);
  }

  @Test(expected = ListDeserializer.SyntaxError.class)
  public void cantDeserializeEmptyString() throws Exception {
    ListDeserializer.deserialize("");
  }

  @Test(expected = ListDeserializer.SyntaxError.class)
  public void cantDeserializeStringThatDoesntStartWithBracket() throws Exception {
    ListDeserializer.deserialize("hello");
  }

  @Test(expected = ListDeserializer.SyntaxError.class)
  public void cantDeserializeStringThatDoesntEndWithBracket() throws Exception {
    ListDeserializer.deserialize("[000000:");
  }

  @Test
  public void emptyList() throws Exception {
    check();
  }

  @Test
  public void listWithOneElement() throws Exception {
    list.add("hello");
    check();
  }

  @Test
  public void listWithTwoElements() throws Exception {
    list.add("hello");
    list.add("world");
    check();
  }

  @Test
  public void listWithSubList() throws Exception {
    List<String> sublist = new ArrayList<String>();
    sublist.add("hello");
    sublist.add("world");
    list.add(sublist);
    list.add("single");
    check();
  }
}
