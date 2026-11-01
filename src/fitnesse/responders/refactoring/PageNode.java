package fitnesse.responders.refactoring;

public class PageNode {
  protected String name;
  protected String path;
  protected boolean hasChildren;

  public PageNode(String name, String path, boolean hasChildren) {
    this.name = name;
    this.path = path;
    this.hasChildren = hasChildren;
  }

  public String toJsonObject() {
    StringBuilder json = new StringBuilder();

    json.append("{");
    json.append("\"name\":\"").append(name).append("\",");
    json.append("\"path\":\"").append(path).append("\",");
    json.append("\"hasChildren\":").append(hasChildren);
    json.append("}");

    return json.toString();
  }
}
