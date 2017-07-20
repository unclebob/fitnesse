// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

package fit;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RowFixture extends ColumnFixture {

  public Object[] results;
  public List<Object> missing = new LinkedList<>();
  public List<Object> surplus = new LinkedList<>();

  @Override
  public void doRows(Parse rows) {
    try {
      bindColumnHeadersToMethodsAndFields(rows.parts);
      results = query();
      match(list(rows.more), list(results), 0);
      Parse last = rows.last();
      last.more = buildRows(surplus.toArray());
      mark(last.more, "surplus");
      mark(missing.iterator(), "missing");
    }
    catch (Exception e) {
      exception(rows.leaf(), e);
    }
  }

  public abstract Object[] query() throws Exception;  // get rows to be compared

  @Override
  public abstract Class<?> getTargetClass();             // get expected type of row

  protected void match(List<?> expected, List<?> computed, int col) {
    if (col >= columnBindings.length) {
      check(expected, computed);
    } else if (columnBindings[col] == null) {
      match(expected, computed, col + 1);
    } else {
      Map<Object, Object> eMap = eSort(expected, col);
      Map<Object, Object> cMap = cSort(computed, col);
      Set<Object> keys = union(eMap.keySet(), cMap.keySet());
      for (Object key : keys) {
        List<?> eList = (List<?>) eMap.get(key);
        List<?> cList = (List<?>) cMap.get(key);
        if (eList == null) {
          surplus.addAll(cList);
        } else if (cList == null) {
          missing.addAll(eList);
        } else if (eList.size() == 1 && cList.size() == 1) {
          check(eList, cList);
        } else {
          match(eList, cList, col + 1);
        }
      }
    }
  }

  protected List<Parse> list(Parse rows) {
    List<Parse> result = new LinkedList<>();
    while (rows != null) {
      result.add(rows);
      rows = rows.more;
    }
    return result;
  }

  protected List<Object> list(Object[] rows) {
    List<Object> result = new LinkedList<>();
    Collections.addAll(result, rows);
    return result;
  }

  protected Map<Object, Object> eSort(List<?> list, int col) {
    TypeAdapter a = columnBindings[col].adapter;
    Map<Object, Object> result = new ConcurrentHashMap<>(list.size());
    for (Object o : list) {
      Parse row = (Parse) o;
      Parse cell = row.parts.at(col);
      try {
        Object key = a.parse(cell.text());
        bin(result, key, row);
      }
      catch (Exception e) {
        exception(cell, e);
        for (Parse rest = cell.more; rest != null; rest = rest.more) {
          ignore(rest);
        }
      }
    }
    return result;
  }

  protected Map<Object, Object> cSort(List<?> list, int col) {
    TypeAdapter a = columnBindings[col].adapter;
    Map<Object, Object> result = new ConcurrentHashMap<>(list.size());
    for (Object row : list) {
      try {
        a.target = row;
        Object key = a.get();
        bin(result, key, row);
      }
      catch (Exception e) {
        // surplus anything with bad keys, including null
        surplus.add(row);
      }
    }
    return result;
  }

  protected void bin(Map<Object, Object> result, Object key, Object row) {
    if (key.getClass().isArray()) {
      key = Arrays.asList((Object[]) key);
    }
    if (result.containsKey(key)) {
      ((List<Object>) result.get(key)).add(row);
    } else {
      List<Object> list = new LinkedList<>();
      list.add(row);
      result.put(key, list);
    }
  }

  protected Set<Object> union(Set<?> a, Set<?> b) {
    Set<Object> result = new HashSet<>();
    result.addAll(a);
    result.addAll(b);
    return result;
  }

  protected void check(List<?> eList, List<?> cList) {
    if (eList.isEmpty()) {
      surplus.addAll(cList);
      return;
    }
    if (cList.isEmpty()) {
      missing.addAll(eList);
      return;
    }
    Parse row = (Parse) eList.remove(0);
    Parse cell = row.parts;
    Object obj = cList.remove(0);
    for (int i = 0; i < columnBindings.length && cell != null; i++) {
      TypeAdapter a = columnBindings[i].adapter;
      if (a != null) {
        a.target = obj;
      }
      check(cell, a);
      cell = cell.more;
    }
    check(eList, cList);
  }

  protected void mark(Parse rows, String message) {
    String annotation = label(message);
    while (rows != null) {
      wrong(rows.parts);
      rows.parts.addToBody(annotation);
      rows = rows.more;
    }
  }

  protected void mark(Iterator<?> rows, String message) {
    String annotation = label(message);
    while (rows.hasNext()) {
      Parse row = (Parse) rows.next();
      wrong(row.parts);
      row.parts.addToBody(annotation);
    }
  }

  protected Parse buildRows(Object[] rows) {
    Parse root = new Parse(null, null, null, null);
    Parse next = root;
    for (Object row : rows) {
      next = next.more = new Parse("tr", null, buildCells(row), null);
    }
    return root.more;
  }

  protected Parse buildCells(Object row) {
    if (row == null) {
      Parse nil = new Parse("td", "null", null, null);
      nil.addToTag(" colspan=" + columnBindings.length);
      return nil;
    }
    Parse root = new Parse(null, null, null, null);
    Parse next = root;
    for (Binding columnBinding : columnBindings) {
      next = next.more = new Parse("td", "&nbsp;", null, null);
      TypeAdapter a = columnBinding.adapter;
      if (a == null) {
        ignore(next);
      } else {
        try {
          a.target = row;
          next.body = gray(escape(a.toString(a.get())));
        }
        catch (Exception e) {
          exception(next, e);
        }
      }
    }
    return root.more;
  }
}
