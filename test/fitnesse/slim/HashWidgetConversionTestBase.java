package fitnesse.slim;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

//Extracted Test class to be implemented by all Java based Slim ports
//The tests for PhpSlim and JsSlim implement this class

public abstract class HashWidgetConversionTestBase {

  private static final String OTHER_INSTANCE = "otherInstance";
  private static final String MY_INSTANCE = "myInstance";
  protected StatementExecutorInterface statementExecutor;

  @Before
  public void setUp() throws Exception {
    statementExecutor = createStatementExecutor();
    createMapReceptorInstance();
  }

  protected abstract StatementExecutorInterface createStatementExecutor() throws Exception;
  
  protected void createMapReceptorInstance() throws Exception {
    statementExecutor.create(MY_INSTANCE, mapReceptorClassName(), new Object[] {});
  }

  protected abstract String mapReceptorClassName();

  protected abstract String mapConstructorClassName();

  private void assertConvertsTo(String string, List<List<String>> list) throws Exception  {
    assertEquals("true", statementExecutor.call(MY_INSTANCE, "setMap", string));
    assertEquals(list, statementExecutor.call(MY_INSTANCE, "query", new Object[] {}));
    statementExecutor.create(OTHER_INSTANCE, mapConstructorClassName(), new Object[] {string});
    assertEquals(list, statementExecutor.call(OTHER_INSTANCE, "query", new Object[] {}));
  }

  @Test
  public void fromEmptyString_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("", new ArrayList<List<String>>());
  }

  @Test
  public void fromGarbageString_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("sdfwewdfsdfwefsdfsdfswe", new ArrayList<List<String>>());
  }

  @Test
  public void fromEmptyTable_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table></table>", new ArrayList<List<String>>());
  }

  @Test
  public void fromTableWithNoColumns_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table><tr></tr><tr></tr></table>", new ArrayList<List<String>>());
  }

  @Test
  public void fromTableWithOneColumn_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table><tr><td>0</td></tr></table>", new ArrayList<List<String>>());
  }

  @Test
  public void fromTableWithMoreThanTwoColumns_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo(
      "<table>" +
        "<tr>" +
        "  <td>0</td>" +
        "  <td>0</td>" +
        "  <td>0</td>" +
        "</tr>" +
        "</table>", new ArrayList<List<String>>());
  }

  @Test
  public void fromTableWithTwoColumnsAndOneRow_shouldCreateMapWithOneEntry() throws Exception {
    assertConvertsTo(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>", asList(asList("name", "Bob")));
  }

  @Test
  public void fromTableWithTwoColumnsAndTwoRows_shouldCreateMapWithTwoEntries() throws Exception {
    assertConvertsTo(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "<tr>" +
        "  <td>address</td>" +
        "  <td>here</td>" +
        "</tr>" +
        "</table>", asList(asList("address", "here"), asList("name", "Bob")));
  }

  @Test
  public void fromTwoValidTables_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>" +
        "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>", new ArrayList<List<String>>());
  }

}
