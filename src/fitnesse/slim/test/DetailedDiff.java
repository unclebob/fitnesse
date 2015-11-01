package fitnesse.slim.test;

public class DetailedDiff {

  private String actualString;
  private int    actualInt;
  private double actualDouble;
  private String actualSymbol;
  
  public void setActualString(String actualString) {
    this.actualString = actualString;
  }
  
  public String expectedString() {
    return actualString;
  }
  
  public void setActualInt(int acutalInt) {
    this.actualInt = acutalInt;
  }
  
  public int expectedInt() {
    return this.actualInt;
  }
  
  public void setActualDouble(double actualDouble) {
    this.actualDouble = actualDouble;
  }
  
  public double expectedDouble() {
    return this.actualDouble;
  }
  
  public void setActualSymbol(String actualSymbol) {
    this.actualSymbol = actualSymbol;
  }
  
  public String expectedSymbol() {
    return actualSymbol;
  }
  
  public String echo(String value) {
    return value;
  }
  
}
