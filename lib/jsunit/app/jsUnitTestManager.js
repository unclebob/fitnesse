/* jsUnit */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Edward Hieatt code.
 *
 * The Initial Developer of the Original Code is
 * Edward Hieatt, edward@jsunit.net.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Edward Hieatt, edward@jsunit.net (original author)
 * Bob Clary, bc@bclary.com
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

function jsUnitTestManager() 
{
  this._windowForAllProblemMessages = null;


  this.container            = top.frames.testContainer
  this.documentLoader       = top.frames.documentLoader;
  this.mainFrame            = top.frames.mainFrame;

  this.containerController = this.container.frames.testContainerController;
  this.containerTestFrame  = this.container.frames.testFrame;

  var mainData             = this.mainFrame.frames.mainData;

  // form elements on mainData frame
  this.testFileName        = mainData.document.testRunnerForm.testFileName;
  this.runButton           = mainData.document.testRunnerForm.runButton;
  this.traceLevel          = mainData.document.testRunnerForm.traceLevel;
  this.closeTraceWindowOnNewRun = mainData.document.testRunnerForm.closeTraceWindowOnNewRun;
  this.timeout             = mainData.document.testRunnerForm.timeout;
  this.setUpPageTimeout      = mainData.document.testRunnerForm.setUpPageTimeout;

  // image output
  this.progressBar         = this.mainFrame.frames.mainProgress.document.progress;

  this.problemsListField           = this.mainFrame.frames.mainErrors.document.testRunnerForm.problemsList;
  this.testCaseResultsField        = this.mainFrame.frames.mainResults.document.resultsForm.testCases;  
  this.resultsTimeField			   = this.mainFrame.frames.mainResults.document.resultsForm.time;  

  // 'layer' output frames
  this.uiFrames                    = new Object();
  this.uiFrames.mainStatus         = this.mainFrame.frames.mainStatus;

  var mainCounts                   = this.mainFrame.frames.mainCounts;

  this.uiFrames.mainCountsErrors   = mainCounts.frames.mainCountsErrors;
  this.uiFrames.mainCountsFailures = mainCounts.frames.mainCountsFailures;
  this.uiFrames.mainCountsRuns     = mainCounts.frames.mainCountsRuns;
  this._baseURL = "";

  this.setup();
}

// seconds to wait for each test page to load
jsUnitTestManager.TESTPAGE_WAIT_SEC  = 20;
jsUnitTestManager.TIMEOUT_LENGTH     = 20;

// seconds to wait for setUpPage to complete
jsUnitTestManager.SETUPPAGE_TIMEOUT    = 60; 

// milliseconds to wait between polls on setUpPages
jsUnitTestManager.SETUPPAGE_INTERVAL   = 100;

jsUnitTestManager.prototype.setup = function ()
{
  this.totalCount    = 0;
  this.errorCount    = 0;
  this.failureCount  = 0;
  this._suiteStack   = Array();


  var initialSuite   = new top.jsUnitTestSuite();
  push(this._suiteStack, initialSuite);
}

jsUnitTestManager.prototype.start = function () 
{
  this._baseURL = this.resolveUserEnteredTestFileName();
  var firstQuery = this._baseURL.indexOf("?");
  if (firstQuery >= 0) {
       this._baseURL = this._baseURL.substring(0, firstQuery);
  }
  var lastSlash = this._baseURL.lastIndexOf("/");
  var lastRevSlash = this._baseURL.lastIndexOf("\\");
  if (lastRevSlash > lastSlash) {
     lastSlash = lastRevSlash;
  }
  if (lastSlash > 0) {
     this._baseURL = this._baseURL.substring(0, lastSlash + 1);
  }

  this._timeRunStarted = new Date();
  this.initialize();
  setTimeout('top.testManager._nextPage();', jsUnitTestManager.TIMEOUT_LENGTH);
}

jsUnitTestManager.prototype.getBaseURL = function () {
  return this._baseURL;
}

jsUnitTestManager.prototype.doneLoadingPage = function (pageName) 
{
  //this.containerTestFrame.setTracer(top.tracer);
  this._testFileName = pageName;
  if (this.isTestPageSuite()) 
    this._handleNewSuite();
  else
  {
    this._testIndex   = 0;
    this._testsInPage = this.getTestFunctionNames();
    this._numberOfTestsInPage = this._testsInPage.length;
    this._runTest();
  }
}

jsUnitTestManager.prototype._handleNewSuite = function () 
{
  var allegedSuite = this.containerTestFrame.suite();
  if (allegedSuite.isjsUnitTestSuite) {
    var newSuite = allegedSuite.clone();
    if (newSuite.containsTestPages())
      push(this._suiteStack, newSuite);
    this._nextPage();
  }
  else {
    alert('Invalid test suite in file ' + this._testFileName);
    this.abort();
  }
}

jsUnitTestManager.prototype._runTest = function () 
{
  if (this._testIndex + 1 > this._numberOfTestsInPage)
  {
    this._nextPage();
    return;
  }

  if (this._testIndex == 0 && typeof(this.containerTestFrame.setUpPage) == 'function')
  {
    // first test for this page and a setUpPage is defined
    if (typeof(this.containerTestFrame.setUpPageStatus) == 'undefined')
    {
      // setUpPage() not called yet, so call it
      this.containerTestFrame.setUpPageStatus = false;
      this.containerTestFrame.startTime = new Date();
      this.containerTestFrame.setUpPage();
      // try test again later
      setTimeout('top.testManager._runTest()', jsUnitTestManager.SETUPPAGE_INTERVAL);
      return;
    }

    if (this.containerTestFrame.setUpPageStatus != 'complete')
    {
      // setUpPage called, but not complete yet
      top.status = 'setUpPage not completed... ' + this.containerTestFrame.setUpPageStatus + ' ' + (new Date());
      if ((new Date() - this.containerTestFrame.startTime) /1000 > this.getsetUpPageTimeout()) {
        alert('setUpPage timed out without completing.');
        if (prompt('Retry or Cancel ?', 'Retry') != 'Retry')
        {
          this.abort();
          return;
        }
        this.containerTestFrame.startTime = (new Date());
      }
      // try test again later
      setTimeout('top.testManager._runTest()', jsUnitTestManager.SETUPPAGE_INTERVAL);
      return;
    }
  }

  top.status = '';
  // either not first test, or no setUpPage defined, or setUpPage completed
  this.executeTestFunction(this._testsInPage[this._testIndex]);
  this.totalCount++;
  this.updateProgressIndicators();
  this._testIndex++;
  setTimeout('top.testManager._runTest()', jsUnitTestManager.TIMEOUT_LENGTH);
}

jsUnitTestManager.prototype._done = function () 
{
  var secondsSinceRunBegan=(new Date() - this._timeRunStarted)/1000;
  this.setStatus('Done (' + secondsSinceRunBegan + ' seconds)');
  this._cleanUp();
  if (top.shouldSubmitResults()) {
    this.resultsTimeField.value = secondsSinceRunBegan;
  	top.submitResults();
  }
}

jsUnitTestManager.prototype._nextPage = function () 
{
  if (this._currentSuite().hasMorePages()) {
    this.loadPage(this._currentSuite().nextPage());
  }
  else {
    pop(this._suiteStack);
    if (this._currentSuite() == null)
      this._done();
    else
      this._nextPage();
  }
}

jsUnitTestManager.prototype._currentSuite = function () 
{
  var suite = null;

  if (this._suiteStack && this._suiteStack.length > 0)
    suite = this._suiteStack[this._suiteStack.length-1];

  return suite;
}

jsUnitTestManager.prototype.calculateProgressBarProportion = function () 
{
  if (this.totalCount == 0) 
    return 0;
  var currentDivisor = 1;
  var result         = 0;
  
  for (var i = 0; i < this._suiteStack.length; i++) {
    var aSuite     = this._suiteStack[i];
    currentDivisor *= aSuite.testPages.length;
    result += (aSuite.pageIndex - 1)/currentDivisor;
  }
  result += (this._testIndex + 1)/(this._numberOfTestsInPage * currentDivisor);
  return result;
}

jsUnitTestManager.prototype._cleanUp = function () 
{
  this.containerController.setTestPage('./app/emptyPage.html');
  this.finalize();
  top.tracer.finalize();
}

jsUnitTestManager.prototype.abort = function () 
{
  this.setStatus('Aborted');
  this._cleanUp();
}

jsUnitTestManager.prototype.getTimeout = function () 
{
  var result = jsUnitTestManager.TESTPAGE_WAIT_SEC;
  try {
    result = eval(this.timeout.value);
  } 
  catch (e) {
  }
  return result;
}

jsUnitTestManager.prototype.getsetUpPageTimeout = function () 
{
  var result = jsUnitTestManager.SETUPPAGE_TIMEOUT;
  try {
    result = eval(this.setUpPageTimeout.value);
  } 
  catch (e) {
  }
  return result;
}

jsUnitTestManager.prototype.isTestPageSuite = function () 
{
  var result = false;
  if (typeof(this.containerTestFrame.suite) == 'function')
  {
    result = true;
  }
  return result;
}

jsUnitTestManager.prototype.getTestFunctionNames = function () 
{
  var testFrame         = this.containerTestFrame;
  var testFunctionNames = new Array();
  var i;
  
  if (testFrame && typeof(testFrame.exposeTestFunctionNames) == 'function')
        return testFrame.exposeTestFunctionNames();
  
  if (testFrame && 
      testFrame.document && 
      typeof(testFrame.document.scripts) != 'undefined') { // IE5 and up
    var scriptsInTestFrame = testFrame.document.scripts;
    
    for (i = 0; i < scriptsInTestFrame.length; i++) {
      var someNames = this._extractTestFunctionNamesFromScript(scriptsInTestFrame[i]);
      if (someNames)
        testFunctionNames=testFunctionNames.concat(someNames);
    }
  } 
  else {
    for (i in testFrame) {
      if (i.substring(0, 4) == 'test' && typeof(testFrame[i]) == 'function')
        push(testFunctionNames, i);
    }
  }
  return testFunctionNames;
}

jsUnitTestManager.prototype._extractTestFunctionNamesFromScript = function (aScript) 
{
  var result;
  var remainingScriptToInspect = aScript.text;
  var currentIndex             = remainingScriptToInspect.indexOf('function test');
  while (currentIndex != -1) {
    if (!result) 
      result=new Array();
      
    var fragment = remainingScriptToInspect.substring(currentIndex, remainingScriptToInspect.length);
    result       = result.concat(fragment.substring('function '.length, fragment.indexOf('(')));
                remainingScriptToInspect=remainingScriptToInspect.substring(currentIndex+12, remainingScriptToInspect.length);
                currentIndex=remainingScriptToInspect.indexOf('function test');
  }
  return result;
}

jsUnitTestManager.prototype.loadPage = function (testFileName) 
{
  this._testFileName         = testFileName;
  this._loadAttemptStartTime = new Date();
  this.setStatus('Opening Test Page "' + this._testFileName + '"');
  this.containerController.setTestPage(this._testFileName);
  this._callBackWhenPageIsLoaded();
}

jsUnitTestManager.prototype._callBackWhenPageIsLoaded = function () 
{
  if ((new Date() - this._loadAttemptStartTime) / 1000 > this.getTimeout()) {
    alert('Reading Test Page ' + this._testFileName + ' timed out.\nMake sure that the file exists and is a Test Page.');
    if (prompt('Retry or Cancel ?', 'Retry') != 'Retry')
    {
      this.abort();
      return;
    }
  }
  if (!this._isTestFrameLoaded()) {
    setTimeout('top.testManager._callBackWhenPageIsLoaded();', jsUnitTestManager.TIMEOUT_LENGTH);
    return;
  }
  this.doneLoadingPage(this._testFileName);
}

jsUnitTestManager.prototype._isTestFrameLoaded = function () 
{
  try {
    return this.containerController.isPageLoaded();
  } 
  catch (e) {
  }
  return false;
}

jsUnitTestManager.prototype.executeTestFunction = function (functionName) 
{
  this._testFunctionName=functionName;
  this.setStatus('Running test "' + this._testFunctionName + '"');
  var excep=null;
  var timeBefore = new Date();  
  try {
    this.containerTestFrame.setUp();
    eval('this.containerTestFrame.' + this._testFunctionName + '();');
  } 
  catch (e1) {
    excep = e1;
  }
  finally {
    try {
      this.containerTestFrame.tearDown();
    } 
    catch (e2) {
      excep = e2;
    }
  }
  var timeTaken = (new Date() - timeBefore) / 1000;
  if (excep != null)
    this._handleTestException(excep);
  var serializedTestCaseString = this._fullyQualifiedCurrentTestFunctionName()+"|"+timeTaken+"|";
  if (excep==null)
  	serializedTestCaseString+="S||";
  else {
  	if (typeof(excep.isJsUnitException) != 'undefined' && excep.isJsUnitException)
  		serializedTestCaseString+="F|";
  	else {
  		serializedTestCaseString+="E|";
  	}
  	serializedTestCaseString+=this._problemDetailMessageFor(excep);
  }  	
  var newOption = new Option(serializedTestCaseString);
  this.testCaseResultsField[this.testCaseResultsField.length]=newOption;  
}

jsUnitTestManager.prototype._fullyQualifiedCurrentTestFunctionName = function() {
    var testURL = this.containerTestFrame.location.href;
    var testQuery = testURL.indexOf("?");
    if (testQuery >= 0) {
        testURL = testURL.substring(0, testQuery);
    }
    if (testURL.substring(0, this._baseURL.length) == this._baseURL) {
          testURL = testURL.substring(this._baseURL.length);
    }
    return testURL + ':' + this._testFunctionName;
}


jsUnitTestManager.prototype._handleTestException = function (excep) 
{
  var problemMessage = this._fullyQualifiedCurrentTestFunctionName() + ' ';
  var errOption;
  if (typeof(excep.isJsUnitException) == 'undefined' || !excep.isJsUnitException) {
    problemMessage += 'had an error';
    this.errorCount++;
  } 
  else {
    problemMessage += 'failed';
    this.failureCount++;
  }
  var listField = this.problemsListField;
  var problemDocument = this.mainFrame.frames.mainErrors.document;
  if (typeof(problemDocument.createElement) != 'undefined') {
    // DOM Level 2 HTML method.
    // this is required for Opera 7 since appending to the end of the 
    // options array does not work, and adding an Option created by new Option()
    // and appended by listField.options.add() fails due to WRONG_DOCUMENT_ERR
    errOption = problemDocument.createElement('option');
    errOption.setAttribute('value', this._problemDetailMessageFor(excep));
    errOption.appendChild(problemDocument.createTextNode(problemMessage));
    listField.appendChild(errOption);
  }
  else {
    // new Option() is DOM 0
    errOption = new Option(problemMessage, this._problemDetailMessageFor(excep));
    if (typeof(listField.add) != 'undefined') {
      // DOM 2 HTML 
      listField.add( errOption , null);
    }
    else if (typeof(listField.options.add) != 'undefined') {
      // DOM 0
      listField.options.add( errOption, null);
    }
    else {
      // DOM 0
      listField.options[listField.length]= errOption;
    }
  }
}

jsUnitTestManager.prototype._problemDetailMessageFor = function (excep) 
{
  var result=null;
  if (typeof(excep.isJsUnitException) != 'undefined' && excep.isJsUnitException) {
    result = '';
    if (excep.comment != null)
      result+=('"'+excep.comment+'"\n');
    
    result += excep.jsUnitMessage;
    
    if (excep.stackTrace)
      result+='\n\nStack trace follows:\n'+excep.stackTrace;
  }
  else {
    result = 'Error message is:\n"';
    result +=
      (typeof(excep.description) == 'undefined') ?
      excep :
      excep.description;
    result += '"';
    if (typeof(excep.stack) != 'undefined') // Mozilla only
      result+='\n\nStack trace follows:\n'+excep.stack;
  }
  return result;
}

jsUnitTestManager.prototype._setTextOnLayer = function (layerName, str)
{
  var html = '';
  html += '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">';
  html += '<html><head><link rel="stylesheet" type="text/css" href="css/jsUnitStyle.css"><\/head>';
  html += '<body><div>';
  html += str;
  html += '<\/div><\/body>';
  html += '<\/html>';
  this.uiFrames[layerName].document.write(html);
  this.uiFrames[layerName].document.close();
}

jsUnitTestManager.prototype.setStatus = function (str)
{
  this._setTextOnLayer('mainStatus', '<b>Status:<\/b> '+str);
}

jsUnitTestManager.prototype._setErrors = function (n)
{
  this._setTextOnLayer('mainCountsErrors', '<b>Errors: <\/b>' + n);
}

jsUnitTestManager.prototype._setFailures = function (n)
{
  this._setTextOnLayer('mainCountsFailures', '<b>Failures:<\/b> ' + n);
}

jsUnitTestManager.prototype._setTotal = function (n)
{
  this._setTextOnLayer('mainCountsRuns', '<b>Runs:<\/b> ' + n);
}

jsUnitTestManager.prototype._setProgressBarImage = function (imgName)
{
  this.progressBar.src=imgName;
}

jsUnitTestManager.prototype._setProgressBarWidth = function (w)
{
  this.progressBar.width=w;
}

jsUnitTestManager.prototype.updateProgressIndicators = function ()
{
  this._setTotal(this.totalCount);
  this._setErrors(this.errorCount);
  this._setFailures(this.failureCount);
  this._setProgressBarWidth(300 * this.calculateProgressBarProportion());

  if (this.errorCount > 0 || this.failureCount > 0)
    this._setProgressBarImage('../images/red.gif');
  else
    this._setProgressBarImage('../images/green.gif');
}

jsUnitTestManager.prototype.showMessageForSelectedProblemTest = function ()
{
  var problemTestIndex = this.problemsListField.selectedIndex;
  if (problemTestIndex != -1)
    alert(this.problemsListField[problemTestIndex].value);
}

jsUnitTestManager.prototype.showMessagesForAllProblemTests = function ()
{
   if (this.problemsListField.length == 0) 
     return;

   try
   {
     if (this._windowForAllProblemMessages && !this._windowForAllProblemMessages.closed)
       this._windowForAllProblemMessages.close();
   }
   catch(e)
   {
   }

   this._windowForAllProblemMessages = window.open('','','width=600, height=350,status=no,resizable=yes,scrollbars=yes');
   var resDoc = this._windowForAllProblemMessages.document;
   resDoc.write('<html><head><link rel="stylesheet" href="../css/jsUnitStyle.css"><title>Tests with problems - JsUnit<\/title><head><body>');
   resDoc.write('<p class="jsUnitSubHeading">Tests with problems (' + this.problemsListField.length + ' total) - JsUnit<\/p>');
   resDoc.write('<p class="jsUnitSubSubHeading"><i>Running on '+navigator.userAgent+'</i></p>');
   for (var i = 0; i < this.problemsListField.length; i++)
   {
     resDoc.write('<p class="jsUnitDefault">');
     resDoc.write('<b>' + (i + 1) + '. ');
     resDoc.write(this.problemsListField[i].text);
     resDoc.write('<\/b><\/p><p><pre>');
     resDoc.write(this.problemsListField[i].value);
     resDoc.write('<\/pre><\/p>');
   }

   resDoc.write('<\/body><\/html>');
   resDoc.close();
}

jsUnitTestManager.prototype._clearProblemsList = function ()
{
  var listField = this.problemsListField;
  var initialLength=listField.options.length;

  for (var i = 0; i < initialLength; i++)
    listField.remove(0);
}

jsUnitTestManager.prototype.initialize = function ()
{
  this.setStatus('Initializing...');
  this._setRunButtonEnabled(false);
  this._clearProblemsList();
  this.updateProgressIndicators();
  this.setStatus('Done initializing');
}

jsUnitTestManager.prototype.finalize = function ()
{
  this._setRunButtonEnabled(true);
}

jsUnitTestManager.prototype._setRunButtonEnabled = function (b)
{
  this.runButton.disabled = !b;
}

jsUnitTestManager.prototype.getTestFileName = function () 
{
  var rawEnteredFileName = this.testFileName.value;
  var result             = rawEnteredFileName;

  while (result.indexOf('\\') != -1)
    result = result.replace('\\', '/');

  return result;
}

jsUnitTestManager.prototype.resolveUserEnteredTestFileName = function (rawText) 
{
  var userEnteredTestFileName = top.testManager.getTestFileName();
  
  // only test for file:// since Opera uses a different format
  if (userEnteredTestFileName.indexOf('http://') == 0 || userEnteredTestFileName.indexOf('https://') == 0 || userEnteredTestFileName.indexOf('file://') == 0)
    return userEnteredTestFileName;
    
  return getTestFileProtocol() + this.getTestFileName();
}

function getTestFileProtocol()
{
  return getDocumentProtocol();
}

function getDocumentProtocol() 
{
  var protocol = top.document.location.protocol;
    
  if (protocol == "file:") 
    return "file:///";

  if (protocol == "http:") 
    return "http://";
    
  if (protocol == 'https:') 
    return 'https://';    
    
  if (protocol == "chrome:") 
    return "chrome://";

  return null;
}

function isBeingRunOverHTTP() {
	return getDocumentProtocol()=="http://";
}   

function getWebserver() {
	if (isBeingRunOverHTTP()) {
		var myUrl = location.href;
		var myUrlWithProtocolStripped = myUrl.substring(myUrl.indexOf("/") + 2);
		return myUrlWithProtocolStripped.substring(0, myUrlWithProtocolStripped.indexOf("/"));
	}
	return null;
}

// the functions push(anArray, anObject) and pop(anArray) 
// exist because the JavaScript Array.push(anObject) and Array.pop() 
// functions are not available in IE 5.0

function push(anArray, anObject) 
{
  anArray[anArray.length]=anObject;
}

function pop(anArray) 
{
  if (anArray.length>=1) {
    delete anArray[anArray.length - 1];
    anArray.length--;
  }
}

if (xbDEBUG.on)
{
  xbDebugTraceObject('window', 'jsUnitTestManager');
  xbDebugTraceFunction('window', 'getTestFileProtocol');
  xbDebugTraceFunction('window', 'getDocumentProtocol');
}
