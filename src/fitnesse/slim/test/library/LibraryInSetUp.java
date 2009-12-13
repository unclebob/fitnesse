package fitnesse.slim.test.library;

public class LibraryInSetUp {
  private boolean echoCalled;

  public void echo() {
    this.echoCalled = true;
  }
  
  public boolean echoInSetUpLibraryCalled() {
    return echoCalled;
  }
}
