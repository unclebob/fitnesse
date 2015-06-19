package fitnesse.testsystems.slim;

import java.util.*;

public class SlimScenarioUsagePer {
    private final String groupName;
    private final Map<String, Integer> usage = new HashMap<String, Integer>();
    private final List<String> overriddenScenarios = new ArrayList<String>();

    public SlimScenarioUsagePer(String groupName) {
        this.groupName = groupName;
    }

    public void addDefinition(String scenarioName) {
        if (usage.containsKey(scenarioName)) {
            overriddenScenarios.add(scenarioName);
        } else {
            addUsage(scenarioName, 0);
        }
    }

    public void addUsage(String scenarioName) {
        addUsage(scenarioName, 1);
    }

    public void addUsage(String scenarioName, Integer valueToAdd) {
        Integer currentValue = usage.get(scenarioName);
        if (currentValue == null) {
            currentValue = 0;
        }
        usage.put(scenarioName, currentValue + valueToAdd);
    }

    public Map<String, Integer> getUsage() {
        return Collections.unmodifiableMap(usage);
    }

    public List<String> getOverriddenScenarios() {
        return overriddenScenarios;
    }

    public String toString() {
        return groupName + ": " + usage;
    }
}
