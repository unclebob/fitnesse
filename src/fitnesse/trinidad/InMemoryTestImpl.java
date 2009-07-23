/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

public class InMemoryTestImpl implements TestDescriptor {
  private String name;
  private String content;

  public String getName() {
    return name;
  }

  public String getContent() {
    return content;
  }

  public InMemoryTestImpl(String name, String content) {
    super();
    this.name = name;
    this.content = content;
  }
}
