package fitnesse.slim.test;

public class Zork extends Object {
  private int i;

  public Zork(int i) {
    this.i = i;
  }

  public int getInt() {
    return i;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Zork) {
      Zork o = (Zork)obj;
      return o.i == i;
    }
    return false;
  }

  public int hashCode() {
    assert false : "hashCode not designed";
  return 42;
  }

}
