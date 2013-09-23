package fit.decorator.util;

import fit.decorator.exceptions.InvalidInputException;

public class Delta {
  private DataType dataType;
  private Object value;

  public Delta(String dataType, String value) throws InvalidInputException {
    this.dataType = DataType.instance(dataType);
    this.value = this.dataType.parse(value);
  }

  public String addTo(String originalValue, int numberofTime) {
    return dataType.addTo(originalValue, value, numberofTime);
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (null == other) {
      return false;
    }
    if (!this.getClass().getName().equals(other.getClass().getName())) {
      return false;
    }
    return this.dataType.equals(((Delta) other).dataType) && this.value.equals(((Delta) other).value);
  }

  public String toString() {
    return dataType.toString() + " and value = " + value;
  }
}
