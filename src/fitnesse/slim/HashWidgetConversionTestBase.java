package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

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
  
  protected void createMapReceptorInstance() {
    Object created = statementExecutor.create(MY_INSTANCE, mapReceptorClassName(), new Object[] {});
    assertEquals("OK", created);
  }

  protected abstract String mapReceptorClassName();

  protected abstract String mapConstructorClassName();

  private void assertConvertsTo(String string, List<Object> list) {
    assertEquals("true", statementExecutor.call(MY_INSTANCE, "setMap", string));
    assertEquals(list, statementExecutor.call(MY_INSTANCE, "query", new Object[] {}));
    Object created = statementExecutor.create(OTHER_INSTANCE, mapConstructorClassName(), new Object[] {string});
    assertEquals("OK", created);
    assertEquals(list, statementExecutor.call(OTHER_INSTANCE, "query", new Object[] {}));
  }

  @Test
  public void fromEmptyString_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("", list());
  }

  @Test
  public void fromGarbageString_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("sdfwewdfsdfwefsdfsdfswe", list());
  }

  @Test
  public void fromEmptyTable_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table></table>", list());
  }

  @Test
  public void fromTableWithNoColumns_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table><tr></tr><tr></tr></table>", list());
  }

  @Test
  public void fromTableWithOneColumn_shouldCreateEmptyMap() throws Exception {
    assertConvertsTo("<table><tr><td>0</td></tr></table>", list());
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
        "</table>", list());
  }

  @Test
  public void fromTableWithTwoColumnsAndOneRow_shouldCreateMapWithOneEntry() throws Exception {
    assertConvertsTo(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>", list(list("name", "Bob")));
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
        "</table>", list(list("address", "here"), list("name", "Bob")));
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
        "</table>", list());
  }

}
