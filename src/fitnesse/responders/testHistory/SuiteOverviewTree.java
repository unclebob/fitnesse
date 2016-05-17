package fitnesse.responders.testHistory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import fitnesse.reporting.history.MostRecentPageHistoryReader;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestResultRecord;
import fitnesse.wiki.WikiPage;
import util.GracefulNamer;

public class SuiteOverviewTree {

  private final TreeItem treeRoot = new TreeItem("root", "");

  public SuiteOverviewTree(List<WikiPage> wikiPagelist) {
    makeTree(convertToPageList(wikiPagelist));
  }

  private void makeTree(List<String> pageList) {
    for (String pageName : pageList)
    {
      String[] splits = pageName.split("\\.");
      treeRoot.addItem(splits, 0);
    }
    compactTree();
  }

  public TreeItem getTreeRoot() {
    return treeRoot;
  }

  private static List<String> convertToPageList(List<WikiPage> wikiPagelist) {
    List<String> allPages = new LinkedList<>();

    for (WikiPage aPage : wikiPagelist)  {
      try {
        allPages.add(aPage.getPageCrawler().getFullPath().toString());
      } catch (Exception e) {
        allPages.add("There was also a probem getting the path of one page.");
      }
    }
    return allPages;
  }

  public void countResults() {
    RecursiveTreeMethod countResults = new RecursiveTreeMethod() {
      @Override
      public boolean shouldDoItemBeforeBranches() {
        return false;
      }

      @Override
      public void doMethod(TreeItem item) {
        item.calculateResults();
      }
    };
    treeRoot.doRecursive(countResults, 0);
  }

  public void findLatestResults(final File historyDirectory) {
    RecursiveTreeMethod findLatestResult = new RecursiveTreeMethod() {
      @Override
      public void doMethod(TreeItem item) {
        if (item.isTest()) {
          File directory = new File(historyDirectory, item.fullName);
          MostRecentPageHistoryReader reader = new MostRecentPageHistoryReader(directory);
          item.result = reader.findMostRecentTestRun();
        }
      }
    };
    treeRoot.doRecursive(findLatestResult, 0);
  }

  private void compactTree() {
    RecursiveTreeMethod compactBranch = new RecursiveTreeMethod() {
      @Override
      public void doMethod(TreeItem item) {
        item.compactWithChildIfOnlyOneChild();
      }
    };
    treeRoot.doRecursive(compactBranch, 0);

  }

  private SimpleDateFormat dateFormatter = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);

  public class TreeItem
  {

    private String name;
    private String fullName;
    int testsPassed = 0;
    int testsUnrun = 0;
    int testsFailed = 0;

    List<TreeItem> branches = new LinkedList<>();
    TestResultRecord result = null;



    public int getTestsPassed() {
      return testsPassed;
    }

    public int getTestsUnrun() {
      return testsUnrun;
    }

    public int getTestsFailed() {
      return testsFailed;
    }

    private double calcPercentOfTotalTests(int value) {
      int totalTests = testsPassed + testsUnrun + testsFailed;
      return ((double)(Math.round(((1000.0 * value)/totalTests))))/10;
    }

    private String makePercentageOfTotalString(int value) {
      double percentage = calcPercentOfTotalTests(value);
      if (calcPercentOfTotalTests(value) < 99.95) {
        return "(" + (int)percentage + "%)";
      }
      return "";
    }

    public String getPassedPercentString() {
      return makePercentageOfTotalString(testsPassed);
    }

    public String getUnrunPercentString() {
      return makePercentageOfTotalString(testsUnrun);
    }

    public String getFailedPercentString() {
      return makePercentageOfTotalString(testsFailed);
    }

    public double getPassedPercent() {
      return calcPercentOfTotalTests(testsPassed);
    }

    public double getUnrunPercent() {
      return calcPercentOfTotalTests(testsUnrun);
    }

    public double getFailedPercent() {
      return calcPercentOfTotalTests(testsFailed);
    }


    public String getName() {
      return GracefulNamer.regrace(name);
    }

    public String getFullName() {
      return fullName;
    }

    public String getHistoryUrl() {
      String url = getFullName();

      if (result != null) {
        url += "?pageHistory&resultDate=";
        url += dateFormatter.format(result.getDate());
      }

      return url;
    }

    public void compactWithChildIfOnlyOneChild() {
      if (branches.size() == 1) {
        TreeItem child = branches.get(0);
        if (!child.isTest()) {
          name += "." + child.name;
          fullName += "." + child.name;
          branches = child.branches;

          compactWithChildIfOnlyOneChild();
        }
      }
    }

    TreeItem(String branchName, String branchFullName) {
      name = branchName;
      fullName = branchFullName;
    }

    public List<TreeItem> getBranches() {
      return branches;
    }

    public void calculateResults() {
      testsPassed = 0;
      testsUnrun = 0;
      testsFailed = 0;

      if (isTest()) {
        if (result == null) {
          testsUnrun++;
        }
        else if ((result.getExceptions() == 0) && (result.getWrong() == 0)) {
          testsPassed++;
        }
        else {
          testsFailed++;
        }
      }
      else {
        for (TreeItem branch : branches) {
          testsUnrun += branch.testsUnrun;
          testsPassed += branch.testsPassed;
          testsFailed += branch.testsFailed;
        }
      }
    }

    @Override
    public String toString() {
      return name;
    }

    void addItem(String[] itemPath, int currentIndex) {
      if (currentIndex < itemPath.length) {
        //special case for this tree only, that all the titles should be organised before we start.
        if (nameSameAsLastName(itemPath[currentIndex])) {
          branches.get(branches.size() - 1).addItem(itemPath, ++currentIndex);
        }
        else {
          String branchName = itemPath[currentIndex];
          String branchFullName = fullName;
          branchFullName += fullName.isEmpty() ? branchName : "." + branchName;
          TreeItem branch = new TreeItem(branchName, branchFullName);
          branches.add(branch);
          branch.addItem(itemPath, ++currentIndex);
        }
      }
    }

    private boolean nameSameAsLastName(String currentName) {
      return !branches.isEmpty() && branches.get(branches.size() - 1).name.equals(currentName);
    }

    public boolean isTest() {
      return (branches.isEmpty());
    }

    public String getCssClass() {
      if (testsFailed != 0) {
        return "fail";
      }
      else if (testsUnrun != 0) {
        return "unrun";
      }
      else {
        return "done";
      }
    }

    void doRecursive(RecursiveTreeMethod method, int level) {
      if (method.shouldDoItemBeforeBranches() && (level != 0)) {
        method.doMethod(this);
      }

      for (TreeItem branch : branches)  {
        branch.doRecursive(method, level + 1);
      }

      if (!method.shouldDoItemBeforeBranches() && (level != 0)) {
        method.doMethod(this);
      }
    }
  }

  abstract class RecursiveTreeMethod
  {
    public boolean shouldDoItemBeforeBranches()
    {
      return true;
    }

    public abstract void doMethod(TreeItem item);

  }
}

