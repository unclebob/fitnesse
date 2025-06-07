// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import fitnesse.slim.SlimIgnoreAllTestsException;
import fitnesse.slim.SlimIgnoreScriptTestException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestSlim implements TestSlimInterface {
  public static final int HUGE_MESSAGE_SIZE = 999999 + 10;
  private boolean niladWasCalled = false;
  private String stringArg;
  private int intArg;
  private double doubleArg;
  private Date dateArg;
  private Integer integerObjectArg;
  private Double doubleObjectArg;
  private char charArg;
  private List<Object> listArg;
  private int constructorArg;
  private String[] stringArray;
  private Integer[] integerArray;
  private Boolean[] booleanArray;
  private Double[] doubleArray;
  private Zork zork;
  private Map<String, String> map;

  public TestSlim() {

  }

  public TestSlim(int constructorArg) {
    this.constructorArg = constructorArg;
  }

  public TestSlim(int constructorArg, TestSlim other) {
    this.constructorArg = constructorArg;
    stringArg = other.getStringArg();
  }

  public TestSlim createTestSlimWithString(String string) {
    TestSlim testSlim = new TestSlim();
    testSlim.setString(string);
    return testSlim;
  }

  @Override
  public String toString() {
    return "TestSlim: " + constructorArg + ", " + stringArg;
  }

  public void nilad() {
    niladWasCalled = true;
  }

  public int returnConstructorArg() {
    return constructorArg;
  }

  public void voidFunction() {

  }

  @Override
  public boolean niladWasCalled() {
    return niladWasCalled;
  }

  public String returnString() {
    return "string";
  }

  public int returnInt() {
    return 7;
  }

  public String returnHugeString() {
    StringBuilder builder = new StringBuilder(HUGE_MESSAGE_SIZE);
    for (int i = 0; i < HUGE_MESSAGE_SIZE; i++) {
      builder.append('x');
    }
    return builder.toString();
  }

  public void setString(String arg) {
    stringArg = arg;
  }

  public void oneString(String arg) {
    stringArg = arg;
  }

  public void oneDate(Date arg) {
    dateArg = new Date(arg.getTime());
  }

  public void oneList(List<Object> l) {
    listArg = l;
  }

  @Override
  public List<Object> getListArg() {
    return listArg;
  }

  @Override
  public String getStringArg() {
    return stringArg;
  }

  @Override
  public Date getDateArg() {
    return new Date(dateArg.getTime());
  }

  public void oneInt(int arg) {
    intArg = arg;
  }

  @Override
  public int getIntArg() {
    return intArg;
  }

  public void oneDouble(double arg) {
    doubleArg = arg;
  }

  @Override
  public double getDoubleArg() {
    return doubleArg;
  }

  public void manyArgs(Integer i, Double d, char c) {
    integerObjectArg = i;
    doubleObjectArg = d;
    charArg = c;
  }

  @Override
  public Integer getIntegerObjectArg() {
    return integerObjectArg;
  }

  @Override
  public double getDoubleObjectArg() {
    return doubleObjectArg;
  }

  @Override
  public char getCharArg() {
    return charArg;
  }

  public int addTo(int a, int b) {
    return a + b;
  }

  public int echoInt(int i) {
    return i;
  }

  public String echoString(String s) {
    return s;
  }

  public void printString(String s) {
    System.out.println(s);
  }

  public List<Object> echoList(List<Object> l) {
    return l;
  }

  public boolean echoBoolean(boolean b) {
    return b;
  }

  public double echoDouble(double d) {
    return d;
  }

  public void execute() {

  }

  public void die() {
    throw new Error("TestSlim died");
  }

  public void setNoSuchConverter(NoSuchConverter x) {

  }

  public NoSuchConverter noSuchConverter() {
    return new NoSuchConverter();
  }

  public void setStringArray(String[] array) {
    stringArray = Arrays.copyOf(array, array.length);
  }

  public String[] getStringArray() {
    return Arrays.copyOf(stringArray, stringArray.length);
  }

  public void setIntegerArray(Integer[] array) {
    integerArray = Arrays.copyOf(array, array.length);
  }

  public Integer[] getIntegerArray() {
    return Arrays.copyOf(integerArray, integerArray.length);
  }

  public Boolean[] getBooleanArray() {
    return Arrays.copyOf(booleanArray, booleanArray.length);
  }

  public void setBooleanArray(Boolean[] booleanArray) {
    this.booleanArray = Arrays.copyOf(booleanArray, booleanArray.length);
  }

  public Double[] getDoubleArray() {
    return Arrays.copyOf(doubleArray, doubleArray.length);
  }

  public void setDoubleArray(Double[] doubleArray) {
    this.doubleArray = Arrays.copyOf(doubleArray, doubleArray.length);
  }

  public String nullString() {
    return null;
  }

  public boolean isSame(Object other) {
    return this == other;
  }

  public String getStringFromOther(TestSlim other) {
    return other.getStringArg();
  }

  public Zork oneZork(Zork zork) {
    this.zork = zork;
    return zork;
  }

  @Override
  public Zork getZork() {
    return zork;
  }

  class NoSuchConverter {
  }

  public boolean throwNormal() throws Exception {
    throw  new Exception("This is my exception");
  }

  public boolean throwStopping() throws Exception {
    throw new StopTestException("This is a stop test exception");
  }

  public boolean throwIgnoreAllStopping() throws Exception {
    throw new SlimIgnoreAllTestsException("This is an ignore all script test exception");
  }

  public boolean throwIgnoreScriptStopping() throws Exception {
    throw new SlimIgnoreScriptTestException("This is an ignore script test exception");
  }

  public boolean throwExceptionWithMessage() throws Exception {
    throw new Exception("message:<<Test message>>");
  }

  public boolean throwStopTestExceptionWithMessage() throws Exception {
    throw new StopTestException("message:<<Stop Test>>");
  }

  public String concatenateThreeArgs(String first, String second, String third) {
    return first + " " + second + " " + third;
  }

  public void setMap(Map<String, String> map) {
    this.map = map;
  }

  public Map<String, String> getMap() {
    return  map;
  }

  @SuppressWarnings("serial")
  class StopTestException extends Exception {
    public StopTestException(String description) {
      super(description);
    }
  }
}
