// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTable extends SlimTable {
  private static final String instancePrefix = "decisionTable";
  protected MethodExtractor setterMethodExtractor;
  protected MethodExtractor getterMethodExtractor;
  protected boolean baselineDecisionTable = false;


  public DecisionTable(Table table, String id, SlimTestContext context) {
    super(table, id, context);
  }


  @Override
  protected String getTableType() {
    return instancePrefix;
  }

  @Override
  public List<SlimAssertion> getAssertions() throws TestExecutionException {
    if (table.getRowCount() == 2)
      throw new SyntaxError("DecisionTables should have at least three rows.");
        // or 1 if only the constructor of a class should be called

    String scenarioName = getScenarioName();
    ScenarioTable scenario = getTestContext().getScenario(scenarioName);
    if (scenario != null) {
      return new ScenarioCaller().call(scenario);
    } else {
      String fixtureName =getFixtureName();
      scenario = getTestContext().getScenario(fixtureName);
      if (scenario != null) {
        return new ScenarioCallerWithConstuctorParameters().call(scenario);
      } else {
       	setterMethodExtractor = prepareMethodExtractorIfNull(setterMethodExtractor,"SLIM_DT_SETTER");
       	getterMethodExtractor = prepareMethodExtractorIfNull(getterMethodExtractor,"SLIM_DT_GETTER");
       	return new FixtureCaller().call(fixtureName);
      }
    }
  }

  private String getScenarioName() {
    StringBuilder nameBuffer = new StringBuilder();
    for (int nameCol = 0; nameCol < table.getColumnCountInRow(0); nameCol += 2) {
      if (nameCol == 0)
        nameBuffer.append(getFixtureName(table.getCellContents(nameCol, 0)));
      else
        nameBuffer.append(table.getCellContents(nameCol, 0));
      nameBuffer.append(" ");
    }
    return Disgracer.disgraceClassName(nameBuffer.toString().trim());
  }

  protected Instruction callAndAssign(String symbolName, String functionName) {
    return callAndAssign(symbolName, getTableName(), functionName);
  }

  private MethodExtractor prepareMethodExtractorIfNull(MethodExtractor current, String sourceVariableName) throws SyntaxError{

  	if (current == null){
  		String setterString = this.getTestContext().getPageToTest().getVariable(sourceVariableName);
  		try{
  		    if (setterString != null && !setterString.isEmpty() ) current = new MethodExtractor(setterString);
  		    else{
  		        current = new MethodExtractor();
  		    }

  		}catch (Exception cause ){
  			SyntaxError sE =  new SyntaxError(sourceVariableName+ " variable could not be parsed:\n"+setterString+"\nCause:"+cause.getMessage());
  			sE.initCause(cause);
  			throw sE;
  		}
  	}
  	return current;
  }

  private class ScenarioCaller extends DecisionTableCaller {
    public ScenarioCaller() {
      super(table, isBaselineDecisionTable());
    }

    public ArrayList<SlimAssertion> call(ScenarioTable scenario) throws TestExecutionException {
    	gatherFunctionsAndVariablesFromColumnHeader();
      ArrayList<SlimAssertion> assertions = new ArrayList<>();
      for (int row = 2; row < table.getRowCount(); row++){
        assertions.addAll(callScenarioForRow(scenario, row));
        assertions.addAll(callFunctions(scenario, row));
      }
      return assertions;
    }

    private List<SlimAssertion> callScenarioForRow(ScenarioTable scenario, int row) throws TestExecutionException {
      checkRow(row);
      return scenario.call(getArgumentsForRow(row), DecisionTable.this, row);
    }

    private List<SlimAssertion> callFunctions(ScenarioTable scenario, int row) throws SyntaxError {
        List<SlimAssertion> instructions = new ArrayList<>();
        for (String functionName : funcStore.getLeftToRightAndResetColumnNumberIterator()) {
          instructions.add(callFunctionInRow(scenario, functionName, row));
        }
        return instructions;
      }

      private SlimAssertion callFunctionInRow(ScenarioTable scenario, String functionName, int row) throws SyntaxError {
        int col = funcStore.getColumnNumber(functionName);
        String name = Disgracer.disgraceMethodName(functionName);
        if (!scenario.getOutputs().contains(name)) {
          throw new SyntaxError(String.format("The argument %s is not an output of the scenario.", name));
        }
        String assignedSymbol = isSymbolAssignment(col, row);
        SlimAssertion assertion;
        if (assignedSymbol != null) {
        	assertion= makeAssertion(callAndAssign(assignedSymbol, getTestContext().getCurrentScriptActor(), "cloneSymbol", "$"+name),
        			new ReturnedSymbolExpectation(col, row, name, assignedSymbol));
        } else {
          assertion = makeAssertion(Instruction.NOOP_INSTRUCTION, new ReturnedSymbolExpectation(getDTCellContents(col, row), col, row, name));
        }
        return assertion;
      }


    private Map<String, String> getArgumentsForRow(int row) {
      Map<String, String> scenarioArguments = new HashMap<>();
      for (String var : constructorParameterStore.getLeftToRightAndResetColumnNumberIterator()) {
          String disgracedVar = Disgracer.disgraceMethodName(var);
          int col = constructorParameterStore.getColumnNumber(var);
          String valueToSet = table.getCellContents(col, 0);
          scenarioArguments.put(disgracedVar, valueToSet);
      }
      for (String var : varStore.getLeftToRightAndResetColumnNumberIterator()) {
        String disgracedVar = Disgracer.disgraceMethodName(var);
        int col = varStore.getColumnNumber(var);
        String valueToSet = getDTCellContents(col, row);
        scenarioArguments.put(disgracedVar, valueToSet);
      }
      return scenarioArguments;
    }
  }

  private class ScenarioCallerWithConstuctorParameters extends ScenarioCaller {
	    public ScenarioCallerWithConstuctorParameters() {
	      super();
	      gatherConstructorParameters();
	    }
  }

  private class FixtureCaller extends DecisionTableCaller {
    public FixtureCaller() {
      super(table,isBaselineDecisionTable() );
    }

    public List<SlimAssertion> call(String fixtureName) throws SyntaxError {
      final List<SlimAssertion> assertions = new ArrayList<>();
      assertions.add(constructFixture(fixtureName));
      assertions.add(makeAssertion(
              callFunction(getTableName(), "table", tableAsList()),
              new SilentReturnExpectation(0, 0)));
      if (table.getRowCount() > 2)
        assertions.addAll(invokeRows());
      return assertions;
    }

    private List<SlimAssertion> invokeRows() throws SyntaxError {
      List<SlimAssertion> assertions = new ArrayList<>();
      assertions.add(callUnreportedFunction("beginTable", 0));
      gatherFunctionsAndVariablesFromColumnHeader();
      for (int row = 2; row < table.getRowCount(); row++)
        assertions.addAll(invokeRow(row));
      assertions.add(callUnreportedFunction("endTable", 0));
      return assertions;
    }

    private List<SlimAssertion> invokeRow(int row) throws SyntaxError {
      List<SlimAssertion> assertions = new ArrayList<>();
      checkRow(row);
      assertions.add(callUnreportedFunction("reset", row));
      assertions.addAll(setVariables(row));
      assertions.add(callUnreportedFunction("execute", row));
      assertions.addAll(callFunctions(row));
      return assertions;
    }

    private SlimAssertion callUnreportedFunction(String functionName, int row) {
      return makeAssertion(callFunction(getTableName(), functionName),
              new SilentReturnExpectation(0, row));
    }

    private List<SlimAssertion> callFunctions(int row) {
      List<SlimAssertion> instructions = new ArrayList<>();
      for (String functionName : funcStore.getLeftToRightAndResetColumnNumberIterator()) {
        instructions.add(callFunctionInRow(functionName, row));
      }
      return instructions;
    }

    private SlimAssertion callFunctionInRow(String functionName, int row) {
      int col = funcStore.getColumnNumber(functionName);
      String assignedSymbol = isSymbolAssignment(col, row);
      SlimAssertion assertion;

      Object[] args = new Object[] {};
      MethodExtractorResult extractedGetter =  getterMethodExtractor.findRule(functionName);
      if(extractedGetter != null){
        functionName = extractedGetter.methodName;
        args = extractedGetter.mergeParameters(args);
      }

      if (assignedSymbol != null) {
        assertion = makeAssertion(callAndAssign(assignedSymbol, getTableName(), functionName, args),
                new SymbolAssignmentExpectation(assignedSymbol, col, row));
      } else {
         assertion = makeAssertion(callFunction(getTableName(), functionName, args),
                new ReturnedValueExpectation(col, row, getDTCellContents(col, row)));
      }
      return assertion;
    }

    private List<SlimAssertion> setVariables(int row) {
      List<SlimAssertion> assertions = new ArrayList<>();
      for (String var : varStore.getLeftToRightAndResetColumnNumberIterator()) {
        int col = varStore.getColumnNumber(var);
        String valueToSet = getDTCellContents(col, row);

        Object[] args = new Object[] {valueToSet};
   	    MethodExtractorResult extractedSetter =  setterMethodExtractor.findRule(var);
   	    if(extractedSetter != null){
          var = extractedSetter.methodName;
          args = extractedSetter.mergeParameters(args);
          }else{
            // Default for Setter
            var = "set " + var;
        }

        Instruction setInstruction = callFunction(getTableName(), var, args);
        assertions.add(makeAssertion(setInstruction,
                new VoidReturnExpectation(col, row)));
      }
      return assertions;
    }
  }

  boolean isBaselineDecisionTable() {
    String useFirstDataRowForEmpty = null;
    useFirstDataRowForEmpty = this.getTestContext().getPageToTest().getVariable("SLIM_DT_BASELINE");
    return ((useFirstDataRowForEmpty != null && !useFirstDataRowForEmpty.isEmpty())
		  || baselineDecisionTable);
  }


  void setBaselineDecisionTable(boolean baselineDecisionTable) {
    this.baselineDecisionTable = baselineDecisionTable;
  }

}
