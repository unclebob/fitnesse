package fitnesse.slim.test;

import java.util.Date;
import java.util.List;

public interface TestSlimInterface {

  boolean niladWasCalled();

  String getStringArg();

  int getIntArg();

  double getDoubleArg();

  List<Object> getListArg();

  Date getDateArg();

  Zork getZork();

  Integer getIntegerObjectArg();

  double getDoubleObjectArg();

  char getCharArg();

}