package fitnesse.junit;

import fitnesse.responders.run.JavaFormatter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class JUnitHelperExampleTest {
  JUnitHelper helper;
  String[] expectedTests = {
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ErikPragtBug",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.HashTableTests.ShouldConvertHashWidgetToHashTable",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.LibrarySuite.LastLibraryPreceedsEarlierLibraryTest",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.LibrarySuite.LibraryInSetUpSuite.LibraryInSetUpAndPageTest",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.LibrarySuite.LibraryInSetUpSuite.LibraryInSetUpTest",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.LibrarySuite.OneLibraryTest",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ParameterizedScenarios",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.BlankCellsInNestedScenariosShouldWork",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.BrotherScenarioLibraryIsIncluded",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.ManyUnclesAreIncluded",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.NoScenarioSectionIfThereAreNone",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.ScenarioLibrariesAreIncludedInTheCorrectOrder",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.ScenarioLibrariesOnlyIncludedInTestPages",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ScenarioLibraryTestSuite.ScenariosOnlyInSlimTests",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SlimSymbolsCanBeBlankOrNull",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SystemUnderTestTest",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TableTableReturnsNull",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestCanPassSymbolsIntoConstructors",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestComparators",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SlimSymbolDollarDollar",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestOrderedQueryWithDuplicateRows",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestPageWithInclude",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestSequentialArgumentProcessing",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestSubsetQuery",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestSymbolsDontGetTurnedToStringsInTheOutput",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.TestTwoIdenticalTablesOnPageDontConflict",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.RegularExpressionMatching",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ChainTest"
  };
  private String[] expectedTestsWithSuiteFilter = new String[]{
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ErikPragtBug",
    "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim"
  };

  @Before
  public void prepare() {
    helper = new JUnitHelper(".",
      new File(System.getProperty("java.io.tmpdir"), "fitnesse").getAbsolutePath());
    JavaFormatter.dropInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
  }

  @Test
  public void assertTestPasses_RunsATestThroughFitNesseAndWeCanInspectTheResultUsingJavaFormatter() throws Exception {
    String testName = "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SystemUnderTestTest";
    helper.assertTestPasses(testName);
    JavaFormatter formatter = JavaFormatter.getInstance(testName);
    Assert.assertEquals(testName, formatter.getTestsExecuted().get(0));
    Assert.assertEquals(1, formatter.getTestsExecuted().size());
  }

//todo There's a better way to do this.  Create a dummy suite instead of using SuiteSlimTests
// @Test
//  public void assertSuitePasses_RunsATestThroughFitNesseAndWeCanInspectTheResultUsingJavaFormatter() throws Exception {
//    helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
//
//    JavaFormatter formatter = JavaFormatter.getInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
//    Assert.assertEquals(new HashSet<String>(Arrays.asList(expectedTests)),
//      new HashSet<String>(formatter.getTestsExecuted()));
//  }

  @Test
  public void assertSuitePasses_appliesSuiteFilterIfDefined() throws Exception {
    helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "testSuite");

    JavaFormatter formatter = JavaFormatter.getInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
    Assert.assertEquals(new HashSet<String>(Arrays.asList(expectedTestsWithSuiteFilter)),
      new HashSet<String>(formatter.getTestsExecuted()));

  }

  @Test
  public void helperWillFailTestsIfNoTestsAreExecuted() throws Exception{
    try{
      helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "nonExistingFilter");
    
    }
    catch (AssertionError ae){
      Assert.assertEquals("at least one test executed", ae.getMessage());
    }
    
    JavaFormatter formatter = JavaFormatter.getInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
    Assert.assertEquals(new HashSet<String>(),
      new HashSet<String>(formatter.getTestsExecuted()));
    
  }
  @Test
  public void dummy() {

  }
}
