package fitnesse.responders.run.slimResponder;

public interface SlimTestContext {
  String getSymbol(String symbolName);

  void setSymbol(String symbolName, String value);

  void addScenario(String scenarioName, ScenarioTable scenarioTable);
}
