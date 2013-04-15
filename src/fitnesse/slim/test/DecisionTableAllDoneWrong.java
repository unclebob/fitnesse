package fitnesse.slim.test;

public class DecisionTableAllDoneWrong {

  public void setFoo(String foo) {
    throw new IllegalArgumentException("sample foo error");
  }

  public void setBar(String bar) {
    throw new UnsupportedOperationException("artificial bar error");
  }

  public String baz() {
    throw new IllegalStateException("dummy baz error");
  }
}
