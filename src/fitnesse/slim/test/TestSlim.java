package fitnesse.slim.test;

import java.util.List;

public class TestSlim {
  private boolean niladWasCalled = false;
  private String stringArg;
  private int intArg;
  private double doubleArg;
  private Integer integerObjectArg;
  private Double doubleObjectArg;
  private char charArg;
  private List<Object> listArg;

  public void nilad() {
    niladWasCalled = true;
  }

  public void voidFunction() {

  }

  public boolean niladWasCalled() {
    return niladWasCalled;
  }

  public String returnString() {
    return "string";
  }

  public int returnInt() {
    return 7;
  }

  public void oneString(String arg) {
    stringArg = arg;
  }

  public void oneList(List<Object> l) {
    listArg = l;
  }

  public List<Object> getListArg() {
    return listArg;
  }

  public String getStringArg() {
    return stringArg;
  }

  public void oneInt(int arg) {
    intArg = arg;
  }

  public int getIntArg() {
    return intArg;
  }

  public void oneDouble(double arg) {
    doubleArg = arg;
  }

  public double getDoubleArg() {
    return doubleArg;
  }

  public void manyArgs(Integer i, Double d, char c) {
    integerObjectArg = i;
    doubleObjectArg = d;
    charArg = c;
  }

  public Integer getIntegerObjectArg() {
    return integerObjectArg;
  }

  public double getDoubleObjectArg() {
    return doubleObjectArg;
  }

  public char getCharArg() {
    return charArg;
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int echoInt(int i) {
    return i;
  }

  public String echoString(String s) {
    return s;
  }

  public List<Object> echoList(List<Object> l) {
    return l;
  }

  public void execute() {
    
  }
}
