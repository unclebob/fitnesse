package fitnesse.plugins.slimcoverage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SlimScenarioUsagePer {
    private final String groupName;
    private final Map<String, AtomicInteger> usage = new LinkedHashMap<>();
    private final List<String> overriddenScenarios = new ArrayList<>();

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
        AtomicInteger currentValue = usage.get(scenarioName);
        if (currentValue == null) {
            currentValue = new AtomicInteger(0);
            usage.put(scenarioName, currentValue);
        }
        currentValue.getAndAdd(valueToAdd);
    }

    public Map<String, Integer> getUsage() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : usage.entrySet()) {
            result.put(entry.getKey(), entry.getValue().intValue());
        }
        return result;
    }

    public List<String> getOverriddenScenarios() {
        return overriddenScenarios;
    }

    @Override
    public String toString() {
        return groupName + ": " + usage;
    }

    public String getGroupName() {
        return groupName;
    }
}
