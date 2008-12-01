package fitnesse.revisioncontrol;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;

public abstract class RevisionControlOperation {
  public static final RevisionControlOperation ADD = new RevisionControlOperation("Add", "addToRevisionControl", "a") {
    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.add(filePath);
    }
  };
  public static final RevisionControlOperation SYNC = new RevisionControlOperation("Synchronize", "syncRevisionControl", "") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.checkState(filePath);
    }
  };
  public static final RevisionControlOperation UPDATE = new RevisionControlOperation("Update", "update", "u") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.update(filePath);
    }
  };
  public static final RevisionControlOperation CHECKOUT = new RevisionControlOperation("Checkout", "checkout", "c") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.checkout(filePath);
    }
  };
  public static final RevisionControlOperation CHECKIN = new RevisionControlOperation("Checkin", "checkin", "i") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.checkin(filePath);
    }
  };
  public static final RevisionControlOperation DELETE = new RevisionControlOperation("Delete", "deleteFromRevisionControl", "d") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.delete(filePath);
    }
  };
  public static final RevisionControlOperation REVERT = new RevisionControlOperation("Revert", "revert", "") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.revert(filePath);
    }
  };
  public static final RevisionControlOperation STATE = new RevisionControlOperation("State", "checkState", "") {

    @Override
    public void execute(RevisionController revisionController, String... filePath) throws RevisionControlException {
      revisionController.checkState(filePath);
    }
  };

  private final String query;
  private final String accessKey;
  private final String name;

  protected RevisionControlOperation(String name, String query, String accessKey) {
    this.name = name;
    this.query = query;
    this.accessKey = accessKey;
  }

  public HtmlTag makeActionLink(String pageName) {
    HtmlUtil.ActionLink link = new HtmlUtil.ActionLink(pageName, name);
    link.setQuery(query);
    link.setShortcutKey(accessKey);
    return link.getHtml();
  }

  public String getName() {
    return name;
  }

  public String getQuery() {
    return query;
  }

  public String getAccessKey() {
    return accessKey;
  }

  @Override
  public String toString() {
    return name;
  }

  public abstract void execute(RevisionController revisionController, String... filePath) throws RevisionControlException;
}
