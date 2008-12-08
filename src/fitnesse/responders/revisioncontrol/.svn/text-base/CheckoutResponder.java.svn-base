package fitnesse.responders.revisioncontrol;

import fitnesse.html.HtmlUtil;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import fitnesse.wiki.FileSystemPage;

public class CheckoutResponder extends RevisionControlResponder {
  public CheckoutResponder() {
    super(CHECKOUT);
  }

  @Override
  protected String responseMessage(String resource) throws Exception {
    return "Click " + HtmlUtil.makeLink(resource + "?edit", "here").html() + " to edit the page.";
  }

  @Override
  protected void performOperation(FileSystemPage page) throws Exception {
    page.execute(CHECKOUT);
  }
}
