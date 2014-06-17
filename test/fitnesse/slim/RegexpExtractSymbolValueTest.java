package fitnesse.slim;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fitnesse.testsystems.slim.tables.TableTable;

public class RegexpExtractSymbolValueTest {

  private List<List<Object>> dataTableTable = new ArrayList<List<Object>>();
  {
    dataTableTable.add(new ArrayList<Object>() {
      {
        add("pass:1");
        add("pass: 2");
        add("error:3");
        add("fail:4");
        add("fail:5str");
        add("ignore:6");
        add("without 7");
        add("with space:6");
      }
    });
  }

  @Test
  public void tableTableExtract() {
    RegexpExtractSymbolValue e = new RegexpExtractSymbolValue(dataTableTable, TableTable.EXTRACT_REGEXP);
    
    assertEquals("1", e.getValue(0, 0));
    assertEquals(" 2", e.getValue(0, 1));
    assertEquals("3", e.getValue(0, 2));
    assertEquals("4", e.getValue(0, 3));
    assertEquals("5str", e.getValue(0, 4));
    assertEquals("6", e.getValue(0, 5));
    assertEquals("without 7", e.getValue(0, 6));
    assertEquals("with space:6", e.getValue(0, 7));
    assertEquals("", e.getValue(0, 8));
    assertEquals("", e.getValue(0, 9));
    assertEquals("", e.getValue(1, 0));
    assertEquals("", e.getValue(2, 0));
  }
}