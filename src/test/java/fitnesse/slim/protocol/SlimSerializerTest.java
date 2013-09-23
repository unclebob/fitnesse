// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.protocol;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import util.ListUtility;

public class SlimSerializerTest {
  private List<Object> list;

  @Before
  public void setUp() {
    list = new ArrayList<Object>();
  }

  @Test
  public void nullListSerialize() throws Exception {
    assertEquals("[000000:]", SlimSerializer.serialize(list));
  }

  @Test
  public void oneItemListSerialize() throws Exception {
    list.add("hello");
    assertEquals("[000001:000005:hello:]", SlimSerializer.serialize(list));
  }

  @Test
  public void twoItemListSerialize() throws Exception {
    list.add("hello");
    list.add("world");
    assertEquals("[000002:000005:hello:000005:world:]", SlimSerializer.serialize(list));
  }

  @Test
  public void serializeNestedList() throws Exception {
    List<String> sublist = new ArrayList<String>();
    sublist.add("element");
    list.add(sublist);
    assertEquals("[000001:000024:[000001:000007:element:]:]", SlimSerializer.serialize(list));
  }

  @Test
  public void serializeListWithNonString() throws Exception {
    String s = SlimSerializer.serialize(ListUtility.<Object>list(1));
    list = SlimDeserializer.deserialize(s);
    assertEquals("1", list.get(0));
  }

  @Test
  public void serializeNullElement() throws Exception {
    List<Object> list = list();
    list.add(null);
    String s = SlimSerializer.serialize(list);
    assertEquals("[000001:000004:null:]", s);
  }


}
