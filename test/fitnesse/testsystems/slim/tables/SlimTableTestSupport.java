package fitnesse.testsystems.slim.tables;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;

/**
 * Test support class for testing slim tables. Class provides
 * the List of {@link #instructions} created and the {@link #tableUnderTest}
 * as protected fields.
 *
 * @param <T> The type of the table under test.
 */
public abstract class SlimTableTestSupport<T extends SlimTable> {

  private WikiPage root;
  protected List<Instruction> instructions;
  protected T tableUnderTest;

  @SuppressWarnings("unchecked")
  private Class<T> getParameterizedClass() {
    Type superclass = this.getClass().getGenericSuperclass();
    if(superclass instanceof ParameterizedType) {
      ParameterizedType type = ParameterizedType.class.cast(superclass);
       return (Class<T>) type.getActualTypeArguments()[0];
    }
    throw new IllegalStateException("Unable to detect the actual type of SlimTable.");
  }

  public T createSlimTable(String tableText) {
    try {
      Constructor<T> constructor = getParameterizedClass().getConstructor(Table.class, String.class, SlimTestContext.class);
      WikiPageUtil.setPageContents(root, tableText);
      String html = root.getData().getHtml();
      TableScanner ts = new HtmlTableScanner(html);
      Table t = ts.getTable(0);
      SlimTestContextImpl testContext = new SlimTestContextImpl();
      return constructor.newInstance(t, "id", testContext);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Instruction>();
  }

  protected T makeSlimTableAndBuildInstructions(String pageContents) throws Exception {
    tableUnderTest = createSlimTable(pageContents);
    instructions.addAll(SlimAssertion.getInstructions(tableUnderTest.getAssertions()));
    return tableUnderTest;
  }
}
