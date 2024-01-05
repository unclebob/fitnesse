package util;

import javax.swing.*;

public class LogViewTest {

  public static void main(String[] args) {
    String line = "Live hoogwater: Nachtelijke controlerondes in Hoorn uit vrees voor gevolgen hoge waterstand Markermeer in combinatie met harde wind ";

    LogView view = new LogView("LogViewTest", null);

    for(int i=0; i<1000; ++i) {
      view.append("" + i + ": " + line + "\n");
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    JOptionPane.showConfirmDialog(null, "That was it.");

    view.dispose();
  }
}
