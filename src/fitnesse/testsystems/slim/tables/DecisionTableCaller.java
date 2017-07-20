package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.slim.Table;


public class DecisionTableCaller {
  protected class ColumnHeaderStore {
    private Map<String, List<Integer>> columnNumbers = new HashMap<>();
    private Map<String, Iterator<Integer>> columnNumberIterator;
    private List<String> leftToRight = new ArrayList<>();

    public void add(String header, int columnNumber) {
      leftToRight.add(header);
      getColumnNumbers(header).add(columnNumber);
    }

    private List<Integer> getColumnNumbers(String header) {
      if (!columnNumbers.containsKey(header)) {
        columnNumbers.put(header, new ArrayList<Integer>());
      }
      return columnNumbers.get(header);
    }

    public int getColumnNumber(String functionName) {
      return columnNumberIterator.get(functionName).next();
    }

    public List<String> getLeftToRightAndResetColumnNumberIterator() {
      resetColumnNumberIterator();
      return leftToRight;
    }

    private void resetColumnNumberIterator() {
      columnNumberIterator = new HashMap<>();
      for (Map.Entry<String, List<Integer>> entry : columnNumbers.entrySet()) {
        columnNumberIterator.put(entry.getKey(), entry.getValue().iterator());
      }
    }
  }

  protected ColumnHeaderStore constructorParameterStore = new ColumnHeaderStore();
  protected ColumnHeaderStore varStore = new ColumnHeaderStore();
  protected ColumnHeaderStore funcStore = new ColumnHeaderStore();
  protected int columnHeaders;
  private final Table table;
  private final boolean emptyCellsUseValueFromFirstDataRow;
  private int firstDataRow = 2;

  public DecisionTableCaller(Table table) {
    this.table = table;
    this.emptyCellsUseValueFromFirstDataRow =false;
  }

  public DecisionTableCaller(Table table, boolean emptyCellsUseValueFromFirstDataRow) {
	this.table = table;
	this.emptyCellsUseValueFromFirstDataRow = emptyCellsUseValueFromFirstDataRow;
  }

  protected void gatherConstructorParameters() {
	    columnHeaders = table.getColumnCountInRow(0);
	    String cell;
	    for (int col = 3; col < columnHeaders; col +=2){
	        cell = table.getCellContents(col-1, 0);
	        constructorParameterStore.add(cell, col);
	    }
  }

  protected void gatherFunctionsAndVariablesFromColumnHeader() {
    columnHeaders = table.getColumnCountInRow(1);
    for (int col = 0; col < columnHeaders; col++)
      putColumnHeaderInFunctionOrVariableList(col);
  }

  private void putColumnHeaderInFunctionOrVariableList(int col) {
    String cell = table.getCellContents(col, 1);
    if (!cell.startsWith("#")) {
      if (cell.endsWith("?") || cell.endsWith("!")) {
        String funcName = cell.substring(0, cell.length() - 1);
        funcStore.add(funcName, col);
      } else {
        varStore.add(cell, col);
      }
    }
  }

  protected String getDTCellContents(int col, int row){
    String value = table.getCellContents(col, row);
    if (shoudUseBaseLineValue(value))
      value = table.getCellContents(col, firstDataRow );
    return value;
  }

  private boolean shoudUseBaseLineValue(String valueToSet) {
    return emptyCellsUseValueFromFirstDataRow && valueToSet != null &&  valueToSet.isEmpty();
  }

  protected void checkRow(int row) throws SyntaxError {
    int columns = table.getColumnCountInRow(row);
    if (columns < columnHeaders)
      throw new SyntaxError(
        String.format("Table has %d header column%s, but row %d only has %d column%s.",
          columnHeaders, plural(columnHeaders), row, columns, plural(columns)));
  }

  private String plural(int n) {
    return n == 1 ? "" : "s";
  }

}
