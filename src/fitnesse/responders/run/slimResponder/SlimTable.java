package fitnesse.responders.run.slimResponder;

import static java.lang.Character.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SlimTable {
  protected Table table;
  protected String id;
  private String tableName;
  private int instructionNumber = 0;
  private List<Object> instructions;
  private boolean isLiteralTable;
  private List<Expectation> expectations = new ArrayList<Expectation>();

  public SlimTable(Table table, String id) {
    this.id = id;
    this.table = table;
    tableName = getTableType() + "_" + id;
    instructions = new ArrayList<Object>();
    isLiteralTable = table.isLiteralTable();
  }

  protected void addExpectation(Expectation e) {
    expectations.add(e);
  }

  protected abstract String getTableType();

  public void appendInstructions(List<Object> instructions) {
    try {
      this.instructions = instructions;
      appendInstructions();
    } catch (Throwable e) {
      String tableName = table.getCellContents(0,0);
      table.setCell(0,0, String.format("!style_fail(!-%s: Bad table: %s-!)", tableName, e.getMessage()));
    }
  }

  public abstract void appendInstructions();

  protected List<Object> prepareInstruction() {
    List<Object> instruction = new ArrayList<Object>();
    instruction.add(makeInstructionTag(instructionNumber));
    instructionNumber++;
    return instruction;
  }

  protected String makeInstructionTag(int instructionNumber) {
    return String.format("%s_%d", tableName, instructionNumber);
  }

  protected int getInstructionNumber() {
    return instructionNumber;
  }

  protected String getTableName() {
    return tableName;
  }

  protected void addInstruction(List<Object> instruction) {
    instructions.add(instruction);
  }

  protected void literalizeTable() {
    if (isLiteralTable) {
      table.setAsNotLiteralTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        for (int col = 0; col < table.getColumnCountInRow(row); col++) {
          table.setCell(col, row, literalize(table.getCellContents(col, row)));
        }
      }
    }
  }

  protected String literalize(String contents) {
    return isLiteralTable ? String.format("!-%s-!", contents) : contents;
  }

  public void evaluateExpectations(Map<String, Object> returnValues) {
    literalizeTable();
    for (Expectation expectation : expectations)
      expectation.evaluateExpectation(returnValues, this);
  }

  static class Disgracer {
    public boolean capitalizeNextWord;
    public StringBuffer disgracedName;
    private String name;

    public Disgracer(String name) {
      this.name = name;
    }

    public static String disgraceClassName(String name) {
      return new Disgracer(name).disgraceClassNameIfNecessary();
    }

    public static String disgraceMethodName(String name) {
      return new Disgracer(name).disgraceMethodNameIfNecessary();
    }

    private String disgraceMethodNameIfNecessary() {
      if (isGraceful()) {
        return disgraceMethodName();
      } else {
        return name;
      }
    }

    private String disgraceMethodName() {
      capitalizeNextWord = false;
      return disgraceName();
    }

    private String disgraceClassNameIfNecessary() {
      if (nameHasDots())
        return name;
      else if (isGraceful()) {
        return disgraceClassName();
      } else {
        return name;
      }
    }

    private String disgraceClassName() {
      capitalizeNextWord = true;
      return disgraceName();
    }

    private boolean nameHasDots() {
      return name.indexOf(".") != -1;
    }

    private String disgraceName() {
      disgracedName = new StringBuffer();
      for (char c : name.toCharArray())
        appendCharInProperCase(c);

      return disgracedName.toString();
    }

    private void appendCharInProperCase(char c) {
      if (isGraceful(c)) {
        capitalizeNextWord = true;
      } else {
        appendProperlyCapitalized(c);
      }
    }

    private void appendProperlyCapitalized(char c) {
      disgracedName.append(capitalizeNextWord ? toUpperCase(c) : toLowerCase(c));
      capitalizeNextWord = false;
    }

    private boolean isGraceful() {
      boolean isGraceful = false;
      for (char c : name.toCharArray()) {
        if (isGraceful(c))
          isGraceful = true;
      }
      return isGraceful;
    }

    private boolean isGraceful(char c) {
      return !(isLetterOrDigit(c) || c == '_');
    }
  }

  public abstract static class Expectation {
    public String expectedValue;
    public int col;
    public int row;
    public int instructionNumber;

    public Expectation( String expected, int instructionNumber, int col, int row) {
      expectedValue = expected;
      this.row = row;
      this.instructionNumber = instructionNumber;
      this.col = col;
    }

    protected void evaluateExpectation(Map<String, Object> returnValues, SlimTable slimTable) {
      String value = (String) returnValues.get(slimTable.makeInstructionTag(instructionNumber));
      String literalizedValue = slimTable.literalize(value);
      String originalContent = slimTable.table.getCellContents(col, row);
      String evaluationMessage = createEvaluationMessage(value, literalizedValue, originalContent);
      slimTable.table.setCell(col, row, evaluationMessage);
    }

    protected abstract String createEvaluationMessage(String value, String literalizedValue, String originalValue);
  }
}
