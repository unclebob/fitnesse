package fitnesse.revisioncontrol;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;

public class RevisionControlOperation {
    public static final RevisionControlOperation ADD = new RevisionControlOperation("Add", "addToRevisionControl", "a");
    public static final RevisionControlOperation SYNC = new RevisionControlOperation("Synchronize", "syncRevisionControl", "");
    public static final RevisionControlOperation UPDATE = new RevisionControlOperation("Update", "update", "u");
    public static final RevisionControlOperation CHECKOUT = new RevisionControlOperation("Checkout", "checkout", "c");
    public static final RevisionControlOperation CHECKIN = new RevisionControlOperation("Checkin", "checkin", "i");
    public static final RevisionControlOperation DELETE = new RevisionControlOperation("Delete", "deleteFromRevisionControl", "d");
    public static final RevisionControlOperation REVERT = new RevisionControlOperation("Revert", "revert", "");
    public static final RevisionControlOperation STATE = new RevisionControlOperation("State", "checkState", "");

    private final String query;
    private final String accessKey;
    private final String name;

    public RevisionControlOperation(String name, String query, String accessKey) {
        this.name = name;
        this.query = query;
        this.accessKey = accessKey;
    }

    public HtmlTag makeActionLink(String pageName) {
        return HtmlUtil.makeActionLink(pageName, name, query, accessKey, false);
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
}
