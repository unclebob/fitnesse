package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SlimTableFactoryTest {
  private SlimTableFactory slimTableFactory;
  private Table table;
  private Map map;

  @Before
  public void setUp() {
    slimTableFactory = new SlimTableFactory();
    table = mock(Table.class);
    map = new HashMap();
    map.put("dt:", DecisionTable.class);
    map.put("dT:", DecisionTable.class);
    map.put("decision:", DecisionTable.class);
    map.put("ordered query:", OrderedQueryTable.class);
    map.put("subset query:", SubsetQueryTable.class);
    map.put("query:", QueryTable.class);
    map.put("table:", TableTable.class);
    map.put("script", ScriptTable.class);
    map.put("scenario", ScenarioTable.class);
    map.put("import", ImportTable.class);
    map.put("something", DecisionTable.class);
    map.put("library", LibraryTable.class);
  }

  @Test
  public void shouldCreateCorrectSlimTableForTablesType() {
    Set entrySet = map.entrySet();

    for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
      Map.Entry entry = (Entry) iterator.next();
      assertThatTableTypeCreateSlimTableType((String) entry.getKey(), (Class) entry.getValue());

    }
  }

  private void assertThatTableTypeCreateSlimTableType(String tableType, Class expectedClass) {
    when(table.getCellContents(0, 0)).thenReturn(tableType);
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, "0", new MockSlimTestContext());
    String message = "should have created a " + expectedClass + " for tabletype: " + tableType
        + " but was " + slimTable.getClass();
    assertThat(message, slimTable, instanceOf(expectedClass));
  }
  
  @Test
  public void checkTableTypeDefineUsedForTable() {
    assertThatTableTypeImportWorks("My query table", "Query", "My query Table", QueryTable.class);
    
    assertThatTableTypeImportWorks("Graceful Difference 1", "Query", "GracefulDifference1", QueryTable.class); 
    assertThatTableTypeImportWorks("GracefulDifference2", "Query", "Graceful Difference 2", QueryTable.class); 

    assertThatTableTypeImportWorks("A Table table", "Table", "A Table table", TableTable.class); 

    assertThatTableTypeImportWorks("As is allowed", "as Table", "As is allowed", TableTable.class); 
    assertThatTableTypeImportWorks("Colon is okay too", "as:Table", "Colon is okay too", TableTable.class);

    assertThatTableTypeImportWorks("", "", "This should be default", DecisionTable.class); 
  }

  @Test
  public void checkTableTypeDefinesIgnoredIfDifferentSpecified() {
    assertThatTableTypeImportWorks("OverrideTable", "as Table", "dt: Override Table", DecisionTable.class);
    
    assertThatTableTypeImportWorks("MakeQuery", "as Table", "query: Make Query", QueryTable.class); 
  }

  
  private void assertThatTableTypeImportWorks(String importName, String importTypedescription, String tableName,
      Class tableClass) {
    addTableTypeImport(importName, importTypedescription);
    assertThatTableTypeCreateSlimTableType(tableName, tableClass);
  }

  private void addTableTypeImport(String tablename, String type) {
    when(table.getCellContents(0, 0)).thenReturn("Define Table Type");
    when(table.getCellContents(0, 1)).thenReturn(tablename);
    when(table.getCellContents(1, 1)).thenReturn(type);
    when(table.getRowCount()).thenReturn(2);
    when(table.getColumnCountInRow(0)).thenReturn(1);
    when(table.getColumnCountInRow(1)).thenReturn(2);
    slimTableFactory.makeSlimTable(table, "0", new MockSlimTestContext());
  }
}
