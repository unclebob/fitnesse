package fitnesse.testsystems.slim.tables;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MethodExtractorRuleTest {


  @Test
  public void JasonConfigurationParser() {
    MethodExtractor cf = new MethodExtractor();
    cf.add(new MethodExtractorRule("abc", "set price", "$2,$1"));
    cf.add(new MethodExtractorRule("(...)/(...)\\s+fixing", "set price", "$2,$1"));
    cf.add(new MethodExtractorRule("Cell (\\d+):(\\d+)", "goto", "$1,$2"));
    cf.add(new MethodExtractorRule("Country\\s+(\\w+)\\s+City\\s+(\\w+)", "geo $1", "$2"));
    cf.add(new MethodExtractorRule("age\\s+(?<person>\\w+)", "setAge $1", "$person"));
    cf.add(new MethodExtractorRule(".+", "set $0", ""));
    String cfString = cf.toJson();
    System.out.println(cfString);
    MethodExtractor cf2 = new MethodExtractor(cfString);
    assertEquals("JSON Objects must match", cfString, cf2.toJson());

    assertEquals("set method Name:[]", cf2.findRule("method Name").toString());
    assertEquals("set price:[USD, EUR]", cf2.findRule("EUR/USD fixing").toString());
    assertEquals("goto:[4, 8]", cf2.findRule("Cell 4:8").toString());
    assertEquals("goto:[9, 2]", cf2.findRule("Cell 9:2").toString());
    assertEquals("geo US:[LA]", cf2.findRule("Country US City LA").toString());
    assertEquals("geo NL:[Amsterdam]", cf2.findRule("Country        NL     City 		Amsterdam").toString());
    assertEquals("setAge Lucy:[Lucy]", cf2.findRule("age Lucy").toString());

  }

  @Test
  public void DynamicDecisionTableSetter() {
    MethodExtractor cf = new MethodExtractor();
    cf.add(new MethodExtractorRule(".+", "set", "$0"));
    assertEquals("set:[method Name]", cf.findRule("method Name").toString());
  }

  @Test
  public void DecisionTableSetter() {
    MethodExtractor cf = new MethodExtractor();
    cf.add(new MethodExtractorRule(".+", "set $0", ""));
    assertEquals("set method Name:[]", cf.findRule("method Name").toString());
  }

  @Test
  public void HybridTableSetter() {
    MethodExtractor cf = new MethodExtractor();
    cf.add(new MethodExtractorRule("(...)/(...)\\s+fixing", "set price", "$2,$1"));
    cf.add(new MethodExtractorRule("Cell (\\d+):(\\d+)", "goto", "$1,$2"));
    cf.add(new MethodExtractorRule("Country\\s+(\\w+)\\s+City\\s+(\\w+)", "geo $1", "$2"));
    cf.add(new MethodExtractorRule("age\\s+(?<person>\\w+)", "setAge $1", "$person"));
    cf.add(new MethodExtractorRule("man\\s+(?<person>\\w+)", "setAge $1", "MR,$person"));
    cf.add(new MethodExtractorRule(".+", "set $0", ""));
    assertEquals("set method Name:[]", cf.findRule("method Name").toString());
    assertEquals("set price:[USD, EUR]", cf.findRule("EUR/USD fixing").toString());
    assertEquals("goto:[4, 8]", cf.findRule("Cell 4:8").toString());
    assertEquals("goto:[9, 2]", cf.findRule("Cell 9:2").toString());
    assertEquals("geo US:[LA]", cf.findRule("Country US City LA").toString());
    assertEquals("geo NL:[Amsterdam]", cf.findRule("Country        NL     City 		Amsterdam").toString());
    assertEquals("setAge Lucy:[MR, Lucy]", cf.findRule("man Lucy").toString());

  }
}
