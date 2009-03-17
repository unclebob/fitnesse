package fit.decorator.util;

import java.awt.Point;

public class PointDataType extends DataType {
  protected String addTo(String originalValue, Object value, int numberofTime) {
    Point originalPoint = ClassDelegatePointParser.parse(originalValue);
    Point pointToBeAdded = (Point) (value);
    for (int i = 0; i < numberofTime; ++i) {
      originalPoint.translate(pointToBeAdded.x, pointToBeAdded.y);
    }
    return "(" + originalPoint.x + "," + originalPoint.y + ")";
  }

  protected Object valueOf(String value) throws Exception {
    return ClassDelegatePointParser.parse(value);
  }
}
