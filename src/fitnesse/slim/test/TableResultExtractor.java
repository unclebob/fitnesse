package fitnesse.slim.test;

import java.util.List;

public class TableResultExtractor {

  public Object getValueFromQueryResultSymbol(List<List<List<Object>>> queryResult, int rowNo, String columnName ){
    List<List<Object>> row =  queryResult.get(rowNo);
    for(int i=0; i< row.size(); i++){
      if(columnName.compareTo( (String) (row.get(i).get(0))) == 0){
        return row.get(i).get(1);
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
