package fitnesse.slim.fixtureInteraction;

import java.util.Date;

public class Testee {

  int i;
  String stringVal;
  Date dateVal;
  double doubleVal;
  float floatVal;
  Integer intWrapperVal;

  public Testee(int i, float floatVal) {
    this.i = i;
    this.floatVal = floatVal;
  }

  public Testee(int i, String stringVal, Date dateVal) {
    this.i = i;
    this.stringVal = stringVal;
    this.dateVal = dateVal;
  }

  public Testee(Integer intWrapperVal, String stringVal, Date dateVal) {
    this.stringVal = stringVal;
    this.dateVal = dateVal;
    this.intWrapperVal = intWrapperVal;
  }

  public Testee(int i, double realVal) {
    this.i = i;
    this.doubleVal = realVal;
  }

  public Testee(String stringVal) {
    this.stringVal = stringVal;
  }

  public Testee(Date dateVal) {
    this.dateVal = dateVal;
  }

  public Testee() {
  }

  public Testee(int i) {
    this.i = i;
  }

  public int getI() {
    return i;
  }

  public void setI(int i) {
    this.i = i;
  }

  public String getStringVal() {
    return stringVal;
  }

  public Date getDateVal() {
    return dateVal;
  }

  public double getDoubleVal() {
    return doubleVal;
  }

  public float getFloatVal() {
    return floatVal;
  }

  public Integer getIntWrapperVal() {
    return intWrapperVal;
  }

}
