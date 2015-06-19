package fitnesse.testsystems.slim;

import java.util.*;

public class SlimScenarioUsage {
    private final Map<String, SlimScenarioUsagePer> usagePerPage = new LinkedHashMap<String, SlimScenarioUsagePer>();

    public SlimScenarioUsagePer getUsageByPage(String pageName) {
        if (!usagePerPage.containsKey(pageName)) {
            usagePerPage.put(pageName, new SlimScenarioUsagePer(pageName));
        }
        return usagePerPage.get(pageName);
    }

    public List<SlimScenarioUsagePer> getUsage() {
        return new ArrayList<SlimScenarioUsagePer>(usagePerPage.values());
    }

    public SlimScenarioUsagePer getScenarioUsage() {
        SlimScenarioUsagePer result = new SlimScenarioUsagePer("Total per scenario");
        for (SlimScenarioUsagePer value : usagePerPage.values()) {
            for (Map.Entry<String, Integer> entry : value.getUsage().entrySet()) {
                result.addUsage(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public Collection<String> getUnusedScenarios() {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Integer> usage : getScenarioUsage().getUsage().entrySet()) {
            if (usage.getValue() < 1) {
                result.add(usage.getKey());
            }
        }
        return result;
    }

    public Collection<String> getUsedScenarios() {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Integer> usage : getScenarioUsage().getUsage().entrySet()) {
            if (usage.getValue() > 0) {
                result.add(usage.getKey());
            }
        }
        return result;
    }

    public Collection<String> getOverriddenScenarios() {
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, Collection<String>> usage : getOverriddenScenariosPerPage().entrySet()) {
            result.addAll(usage.getValue());
        }
        return result;
    }

    public Map<String, Collection<String>> getOverriddenScenariosPerPage() {
        Map<String, Collection<String>> result = new LinkedHashMap<String, Collection<String>>();
        for (Map.Entry<String, SlimScenarioUsagePer> value : usagePerPage.entrySet()) {
            if (!value.getValue().getOverriddenScenarios().isEmpty()) {
                result.put(value.getKey(), value.getValue().getOverriddenScenarios());
            }
        }
        return result;
    }

    public Map<String, Collection<String>> getPagesUsingScenario() {
        Map<String, Collection<String>> result = new LinkedHashMap<String, Collection<String>>();
        for (Map.Entry<String, SlimScenarioUsagePer> value : usagePerPage.entrySet()) {
            String page = value.getKey();
            for (Map.Entry<String, Integer> entry : value.getValue().getUsage().entrySet()) {
                String scenario = entry.getKey();
                Collection<String> pagesUsingScenario = getPagesForScenario(result, scenario);
                pagesUsingScenario.add(page);
            }
        }
        return result;
    }

    protected Collection<String> getPagesForScenario(Map<String, Collection<String>> result, String page) {
        Collection<String> pagesUsingScenario = result.get(page);
        if (pagesUsingScenario == null) {
            pagesUsingScenario = new ArrayList<String>();
            result.put(page, pagesUsingScenario);
        }
        return pagesUsingScenario;
    }

    public String toString() {
        return "ScenarioUsage: " + usagePerPage;
    }
}
