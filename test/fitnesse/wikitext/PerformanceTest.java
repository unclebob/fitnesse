package fitnesse.wikitext;

import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.protocol.SlimDeserializer;

import fitnesse.slim.protocol.SlimSerializer;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PerformanceTest {
    private final String tablePageContent;
    private final String definePageContent;

    public PerformanceTest() {
        StringBuilder table = new StringBuilder();
        StringBuilder define = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
          table.append("|aaaaaaaaaa|bbbbbbbbbb|cccccccccc|dddddddddd|eeeeeeeeee|ffffffffff|gggggggggg|hhhhhhhhhh|iiiiiiiiiii|jjjjjjjjjj|kkkkkkkkkk|lllllllllll|mmmmmmmmmm|nnnnnnnnnn|oooooooooo|pppppppppp|qqqqqqqqqq|rrrrrrrrrr|ssssssssss|tttttttttt|uuuuuuuuuu|vvvvvvvvvv|wwwwwwwwww|xxxxxxxxxx|yyyyyyyyyy|zzzzzzzzzz|\n");
          define.append("!define variable").append(i).append(" {aaaaaaaaaa bbbbbbbbbb cccccccccc dddddddddd eeeeeeeeee ffffffffff gggggggggg hhhhhhhhhh iiiiiiiiii jjjjjjjjjj kkkkkkkkkk llllllllll mmmmmmmmmm nnnnnnnnnn oooooooooo pppppppppp qqqqqqqqqq rrrrrrrrrr ssssssssss tttttttttt uuuuuuuuuu vvvvvvvvvv wwwwwwwwww xxxxxxxxxx yyyyyyyyyy zzzzzzzzzz}\n");
        }
        tablePageContent = table.toString();
        definePageContent = define.toString();
    }

    @Test
    public void NewParserTable() {
        runNewParser("big table", tablePageContent);
    }

  @Test
  public void NewParserDefine() {
    runNewParser("big define", definePageContent);
  }

  @Test
  public void Collapsible() {
    StringBuilder collapsible = new StringBuilder();
    for (int i=0; i<100; i++) collapsible.append("!* title\nsomething\n");
    runNewParser("big collapsible", collapsible.toString());
  }

  @Test
  public void Match() {
    SymbolStream symbols = new SymbolStream();
    Matcher matcher = new Matcher();
    StringBuilder input = new StringBuilder();
    for (int i=0; i<1000; i++) {
      matcher.string("abc");
      input.append("abc");
    }
    for (int i=0; i < 10000; i++)
      matcher.makeMatch(new ScanString(input.toString(), 0), symbols);
  }

  @Test
    public void ParserDefineTable() {
      StringBuilder input = new StringBuilder();
      for (int i = 0; i < 20; i++) {
        input.append("!define x").append(i).append(" {|a|\n|b|}\n");
      }
      runNewParser("define table", input.toString());
    }

    private void runNewParser(String name, String input) {
        long start = System.currentTimeMillis();
        WikiPage page = new TestRoot().makePage("NewTest");
        //String result = ParserTest.translateTo(new TestRoot().makePage("NewTest"), pageContent);
        SyntaxTreeV2 syntaxTree = new SyntaxTreeV2();
        syntaxTree.parse(input, new ParsingPage(new WikiSourcePage(page)));
        System.out.println(name + " parse " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        /*String result =*/ syntaxTree.translateToHtml();
        System.out.println(name + " render " + (System.currentTimeMillis() - start));
        //System.out.println(result);
        assertEquals("done", "done");
    }

    /** For dramatic effect, run in debug mode */
    @Test
    public void listDeserializationTest() {
      List<Object> objects = new ArrayList<>();
      for (int i = 0; i < 10000; i++) {
        objects.add("This is string " + i);
      }
      final String serializedList = SlimSerializer.serialize(objects);

      long start = System.currentTimeMillis();
      List<Object> result = SlimDeserializer.deserialize(serializedList);
      System.out.println("deserialize " + (System.currentTimeMillis() - start));

      assertEquals(objects, result);
    }

}
