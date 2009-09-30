package fitnesse.slim.test;

import java.util.Date;
import java.util.List;

public interface TestSlimInterface {

  public abstract boolean niladWasCalled();

  public abstract String getStringArg();

  public abstract int getIntArg();

  public abstract double getDoubleArg();

  public abstract List<Object> getListArg();

  public abstract Date getDateArg();

  public abstract Zork getZork();

  public abstract Integer getIntegerObjectArg();

  public abstract double getDoubleObjectArg();

  public abstract char getCharArg();

}