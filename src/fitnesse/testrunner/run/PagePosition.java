package fitnesse.testrunner.run;

import java.util.Arrays;
import java.util.List;

public class PagePosition {
  private final List<Object> group;
  private final int positionInGroup;

  public PagePosition(int positionInGroup, Object... group) {
    this(Arrays.asList(group), positionInGroup);
  }

  public PagePosition(List<Object> group, int positionInGroup) {
    this.group = group;
    this.positionInGroup = positionInGroup;
  }

  public List<Object> getGroup() {
    return group;
  }

  public Integer getGroupIntValue(int index) {
    Object partO = getGroup().get(index);
    if (partO instanceof String) {
      partO = Integer.valueOf((String) partO);
    }
    return (Integer) partO;
  }

  public int getPositionInGroup() {
    return positionInGroup;
  }

  @Override
  public String toString() {
    return group + ": " + positionInGroup;
  }
}
