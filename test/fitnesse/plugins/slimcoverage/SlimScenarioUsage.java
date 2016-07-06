package fitnesse.plugins.slimcoverage;

import java.util.*;

import org.apache.commons.lang.StringUtils;

public class SlimScenarioUsage {
    private final Map<String, SlimScenarioUsagePer> usagePerPage = new LinkedHashMap<>();

    public SlimScenarioUsagePer getUsageByPage(String pageName) {
        if (!usagePerPage.containsKey(pageName)) {
            usagePerPage.put(pageName, new SlimScenarioUsagePer(pageName));
        }
        return usagePerPage.get(pageName);
    }

    public List<SlimScenarioUsagePer> getUsage() {
        return new ArrayList<>(usagePerPage.values());
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
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> usage : getScenarioUsage().getUsage().entrySet()) {
            if (usage.getValue() < 1) {
                result.add(usage.getKey());
            }
        }
        return result;
    }

    public Collection<String> getUsedScenarios() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> usage : getScenarioUsage().getUsage().entrySet()) {
            if (usage.getValue() > 0) {
                result.add(usage.getKey());
            }
        }
        return result;
    }

    public Collection<String> getOverriddenScenarios() {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Collection<String>> usage : getOverriddenScenariosPerPage().entrySet()) {
            result.addAll(usage.getValue());
        }
        return result;
    }

    public Map<String, Collection<String>> getOverriddenScenariosPerPage() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, SlimScenarioUsagePer> value : usagePerPage.entrySet()) {
            if (!value.getValue().getOverriddenScenarios().isEmpty()) {
                result.put(value.getKey(), value.getValue().getOverriddenScenarios());
            }
        }
        return result;
    }

    public Map<String, Collection<String>> getPagesUsingScenario() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, SlimScenarioUsagePer> value : usagePerPage.entrySet()) {
            String page = value.getKey();
            for (Map.Entry<String, Integer> entry : value.getValue().getUsage().entrySet()) {
                if (entry.getValue() > 0) {
                    String scenario = entry.getKey();
                    Collection<String> pagesUsingScenario = getOrCreateCollection(result, scenario);
                    pagesUsingScenario.add(page);
                }
            }
        }
        return result;
    }

    public Map<String, Collection<String>> getScenariosBySmallestScope() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        Map<String, Collection<String>> pagesPerScenario = getPagesUsingScenario();
        for (Map.Entry<String, Collection<String>> ppsEntry : pagesPerScenario.entrySet()) {
            String scenario = ppsEntry.getKey();
            Collection<String> pages = ppsEntry.getValue();
            String scope = getLongestSharedPath(pages);
            Collection<String> scenariosForScope = getOrCreateCollection(result, scope);
            scenariosForScope.add(scenario);
        }
        return result;
    }

    private String getLongestSharedPath(Collection<String> pages) {
        String result;
        if (pages.size() == 1) {
            result = pages.iterator().next();
        } else {
            List<String> pageNames = new ArrayList<>(pages);
            String longestPrefix = StringUtils.getCommonPrefix(pageNames.toArray(new String[pageNames.size()]));
            if (longestPrefix.endsWith(".")) {
                result = longestPrefix.substring(0, longestPrefix.lastIndexOf("."));
            } else {
                if (pageNames.contains(longestPrefix)) {
                    result = longestPrefix;
                } else {
                    int lastDot = longestPrefix.lastIndexOf(".");
                    result = longestPrefix.substring(0, lastDot);
                }
            }
        }
        return result;
    }

    protected Collection<String> getOrCreateCollection(Map<String, Collection<String>> map, String scope) {
        Collection<String> value = map.get(scope);
        if (value == null) {
            value = new ArrayList<>();
            map.put(scope, value);
        }
        return value;
    }

    @Override
    public String toString() {
        return "ScenarioUsage: " + usagePerPage;
    }
}
