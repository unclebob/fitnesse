// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

import fitnesse.http.Request;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.reporting.history.SuiteXmlReformatter;
import fitnesse.reporting.history.SuiteHistoryFormatter;
import fitnesse.responders.MockWikiImporter;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testsystems.TestSystemListener;

public class SuiteResponder extends TestResponder {

  public SuiteResponder() {
  }

  public SuiteResponder(MockWikiImporter mockWikiImporter) {
    super(mockWikiImporter);
  }


}
