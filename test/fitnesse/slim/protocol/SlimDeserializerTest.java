// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.protocol;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SlimDeserializerTest {
  private List<Object> list;

  @Before
  public void setUp() {
    list = new ArrayList<>();
  }

  private void check() {
    String serialized = SlimSerializer.serialize(list);
    List<Object> deserialized = SlimDeserializer.deserialize(serialized);
    Assert.assertEquals(list, deserialized);
  }

  @Test(expected = SyntaxError.class)
  public void cantDeserializeNullString() throws Exception {
    SlimDeserializer.deserialize(null);
  }

  @Test(expected = SyntaxError.class)
  public void cantDeserializeEmptyString() throws Exception {
    SlimDeserializer.deserialize("");
  }

  @Test(expected = SyntaxError.class)
  public void cantDeserializeStringThatDoesntStartWithBracket() throws Exception {
    SlimDeserializer.deserialize("hello");
  }

  @Test(expected = SyntaxError.class)
  public void cantDeserializeStringThatDoesntEndWithBracket() throws Exception {
    SlimDeserializer.deserialize("[000000:");
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
    List<String> sublist = new ArrayList<>();
    sublist.add("hello");
    sublist.add("world");
    list.add(sublist);
    list.add("single");
    check();
  }

  @Test
  public void listWithElementsWithBrackets() throws Exception {
    list.add("hello");
    list.add("[world, world2]");
    check();
  }
}
