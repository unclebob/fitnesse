package fitnesse.slim.fixtureInteraction;

import java.util.Date;

public class Testee {

  int intVal;
  String stringVal;
  Date dateVal;
  double doubleVal;
  float floatVal;

  public Testee(int intVal, float floatVal) {
    this.intVal = intVal;
    this.floatVal = floatVal;
  }

  public Testee(int intVal, String stringVal, Date dateVal) {
    this.intVal = intVal;
    this.stringVal = stringVal;
    this.dateVal = dateVal;
  }

  public Testee(int intVal, double realVal) {
    this.intVal = intVal;
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

  public Testee(int intVal) {
    this.intVal = intVal;
  }

  public int getIntVal() {
    return intVal;
  }

  public void setIntVal(int intVal) {
    this.intVal = intVal;
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
}
