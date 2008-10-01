package fitnesse.responders.run.slimResponder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SlimTable {
  protected Table table;
  protected String id;
  private String tableName;
  private int instructionNumber = 0;
  private List<Object> instructions;

  public SlimTable(Table table, String id) {
    this.id = id;
    this.table = table;
    tableName = getTableType() + "_" + id;
    instructions = new ArrayList<Object>();
  }

  protected abstract String getTableType();

  public void appendInstructions(List<Object> instructions) {
    this.instructions = instructions;
    appendInstructions();
  }

  public abstract void appendInstructions();

  public abstract void evaluateExpectations(Map<String, Object> returnValues);

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
}
