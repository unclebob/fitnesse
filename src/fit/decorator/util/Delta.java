package fit.decorator.util;

import fit.decorator.exceptions.InvalidInputException;

public class Delta {
  private final DataType dataType;
  private final Object value;

  public Delta(String dataType, String value) throws InvalidInputException {
    this.dataType = DataType.instance(dataType);
    this.value = this.dataType.parse(value);
  }

  public String addTo(String originalValue, int numberofTime) {
    return dataType.addTo(originalValue, value, numberofTime);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (null == other) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    return this.dataType.equals(((Delta) other).dataType) && this.value.equals(((Delta) other).value);
  }

  @Override
  public int hashCode() {
    return this.getClass().getName().hashCode();
  }

  @Override
  public String toString() {
    return dataType.toString() + " and value = " + value;
  }
}
