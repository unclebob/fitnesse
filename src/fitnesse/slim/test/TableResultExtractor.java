package fitnesse.slim.test;

import java.util.List;

public class TableResultExtractor {

  public Object getValueFromQueryResultSymbol(List<List<List<Object>>> queryResult, int rowNo, String columnName ){
    List<List<Object>> row =  queryResult.get(rowNo);
    for (List<Object> aRow : row) {
      if (columnName.compareTo((String) (aRow.get(0))) == 0) {
        return aRow.get(1);
      }
    }
    throw new RuntimeException("No column with name '" + columnName + "' found in row " + rowNo);

  }
  public Object getValueFromTableResultSymbol(List<List<Object>> tableResult, int rowNo, int columnNo ){
    return  tableResult.get(rowNo).get(columnNo);
  }

  public Object freeSymbol(){
    return null;
  }

}
