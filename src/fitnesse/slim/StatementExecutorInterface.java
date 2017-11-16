package fitnesse.slim;

import fitnesse.slim.instructions.InstructionExecutor;

public interface StatementExecutorInterface extends InstructionExecutor {

  /**
   * This method can be used by TableTable custom fixtures to have access
   * to the table of symbols. This enables elaborate fixtures that can
   * both assign and resolve any symbols on their own.
   *
   * Have a look to this FitNesse page for some examples:
   * FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableSuite.SymbolsInTableTableManagedByTheFixture
   *
   * Please note: this method returns a String version (after conversion by converter as configured in
   * {@link fitnesse.slim.converters.ConverterRegistry}) of the symbol's value, unless it is a List.
   * For Lists it returns the actual List.
   * To access the actual/raw object use {@link #getSymbolObject(String)}.
   *
   * @param symbolName name of symbol to retrieve value for.
   * @return value of symbol, after conversion by {@link fitnesse.slim.converters.ConverterRegistry}.
   */
  Object getSymbol(String symbolName);

  /**
   * This method can be used by TableTable custom fixtures to have access
   * to the table of symbols. This enables elaborate fixtures that can
   * both assign and resolve any symbols on their own.
   *
   * Have a look to this FitNesse page for some examples:
   * FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableSuite.SymbolsInTableTableManagedByTheFixture
   *
   * This method is similar to {@link #getSymbol(String)}, but it always returns the Object for the object, without
   * conversion by converter as configured in {@link fitnesse.slim.converters.ConverterRegistry}.
   *
   * @param symbolName name of symbol to retrieve value for.
   * @return value of symbol.
   */
  Object getSymbolObject(String symbolName);

  Object getInstance(String instanceName);

  boolean stopHasBeenRequested();

  void reset();

  void setInstance(String actorInstanceName, Object actor);
}
