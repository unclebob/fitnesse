<?xml version="1.0"?>
<testResults>
	<host>localhost:8080</host>
	<rootPath>FitNesse.SuiteAcceptanceTests.SuiteFixtureTests</rootPath>

	<result>
		<relativePageName>SuiteColumnFixtureSpec.TestArraysInColumnFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-2395642552201489934');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-2395642552201489934"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-2395642552201489934"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>You can put arrays of objects into ColumnFixture<a title="create page" href="FitNesse.SuiteAcceptanceTests.SuiteFixtureTests.SuiteColumnFixtureSpec.ColumnFixture?edit&amp;nonExistent=true">[?]</a> fixtures.</h3><br/><table border="1" cellspacing="0">
<tr><td colspan="3">fitnesse.fixtures.ComplexAddFixture</td>
</tr>
<tr><td>a</td>
<td>b</td>
<td>sum?</td>
</tr>
<tr><td>1,2</td>
<td>3,4</td>
<td class="pass">4,6</td>
</tr>
</table>
<br/><pre>
public class ComplexAddFixture extends ColumnFixture
{
  public int[] a;
  public int[] b;
  public int[] sum() {
    return new int[] {a[0]+b[0], a[1]+b[1]};
  }
}
</pre><br/><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('5328107532725872232');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img5328107532725872232"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="5328107532725872232"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>1</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteColumnFixtureSpec.TestMissingField</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('5540970778229974936');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img5540970778229974936"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="5540970778229974936"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a column fixture, if the header row mentions a field that is not in the fixture, then the following message should appear in that cell:</h3><pre>Could not find field: fieldname.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">ColumnFixtureTestFixture is a special class used for testing Column fixtures.</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Import|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|fitnesse.fixtures|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>&nbsp;</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Column fixture test fixture|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|no such field|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>ColumnFixtureTestPage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>ColumnFixtureTestPage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Could not find field: no such field</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

870
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: ColumnFixtureTestPage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="ColumnFixtureTestPage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ColumnFixtureTestPage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ColumnFixtureTestPage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ColumnFixtureTestPage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ColumnFixtureTestPage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ColumnFixtureTestPage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/ColumnFixtureTestPage" class="page_title"&gt;ColumnFixtureTestPage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

d5
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Import&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;fitnesse.fixtures&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
d9

&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Column fixture test fixture&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td class="error"&gt;no such field&lt;hr/&gt; &lt;span class="fit_label"&gt;Could not find field: no such field.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

cc
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.ColumnFixtureTestPage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.ColumnFixtureTestPage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-139973487097662674');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-139973487097662674"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-139973487097662674"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteColumnFixtureSpec.TestMissingMethod</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('8458493008057611721');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img8458493008057611721"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="8458493008057611721"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a column fixture, if the header row mentions a method that is not in the fixture, then the following message should appear in that cell:</h3><pre>Could not find method: methodName.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">ColumnFixtureTestFixture is a special class used for testing Column fixtures.</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Import|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|fitnesse.fixtures|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>&nbsp;</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Column fixture test fixture|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|no such method()|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>ColumnFixtureTestPage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response requester</td>
</tr>
<tr><td>uri</td>
<td>contents?</td>
</tr>
<tr><td>ColumnFixtureTestPage</td>
<td>&nbsp; <span class="fit_grey"><pre>HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Cache-Control: max-age=0
Content-Length: 2434
Connection: close
Server: FitNesse-v20080908

&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;ColumnFixtureTestPage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="ColumnFixtureTestPage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ColumnFixtureTestPage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ColumnFixtureTestPage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ColumnFixtureTestPage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ColumnFixtureTestPage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ColumnFixtureTestPage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;span class="page_title"&gt;ColumnFixtureTestPage&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Import&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;fitnesse.fixtures&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Column fixture test fixture&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;no such method()&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;div class="footer"&gt;
&lt;/div&gt;
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;
</pre></span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>Response examiner</td>
</tr>
<tr><td>contents?</td>
</tr>
<tr><td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Cache-Control: max-age=0
Content-Length: 2434
Connection: close
Server: FitNesse-v20080908

&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;ColumnFixtureTestPage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="ColumnFixtureTestPage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ColumnFixtureTestPage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ColumnFixtureTestPage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ColumnFixtureTestPage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ColumnFixtureTestPage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ColumnFixtureTestPage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;span class="page_title"&gt;ColumnFixtureTestPage&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Import&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;fitnesse.fixtures&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Column fixture test fixture&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;no such method()&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;div class="footer"&gt;
&lt;/div&gt;
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;
</span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>ColumnFixtureTestPage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Could not find method: no such method()</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

870
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: ColumnFixtureTestPage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="ColumnFixtureTestPage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ColumnFixtureTestPage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ColumnFixtureTestPage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ColumnFixtureTestPage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ColumnFixtureTestPage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ColumnFixtureTestPage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/ColumnFixtureTestPage" class="page_title"&gt;ColumnFixtureTestPage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

d5
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Import&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;fitnesse.fixtures&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
e0

&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Column fixture test fixture&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td class="error"&gt;no such method()&lt;hr/&gt; &lt;span class="fit_label"&gt;Could not find method: no such method().&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

cc
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.ColumnFixtureTestPage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.ColumnFixtureTestPage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-408349771874838842');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-408349771874838842"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-408349771874838842"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteColumnFixtureSpec.TestSaveAndRecallSymbol</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('5078139212249761650');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img5078139212249761650"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="5078139212249761650"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>You can save and recall symbols in a ColumnFixture.  You do this by using the =id? and id= syntax.</h3><br/><ul>
	<li>=id? or =id() takes the output of a function and stores it in the symbol named by the cell.  In the example below the integer 1 is stored in the symbol <i>one</i>, and the integer 2 is stored in the symbol <i>two</i>.</li>
	<li>id= recalls the value of the symbol named by the cell, and puts it in the <i>id</i> variable.</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">fitnesse.fixtures.ColumnFixtureTestFixture</td>
</tr>
<tr><td>input</td>
<td>=output?</td>
</tr>
<tr><td>1</td>
<td>one <span class="fit_grey"> = 1</span></td>
</tr>
<tr><td>2</td>
<td>two <span class="fit_grey"> = 2</span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">fitnesse.fixtures.ColumnFixtureTestFixture</td>
</tr>
<tr><td>input=</td>
<td>output?</td>
</tr>
<tr><td>one <span class="fit_grey"> = 1</span></td>
<td class="pass">1</td>
</tr>
<tr><td>two <span class="fit_grey"> = 2</span></td>
<td class="pass">2</td>
</tr>
</table>
<br/><b>With classed integral types, there's a chance the value may be null as a correct result:</b><br/><table border="1" cellspacing="0">
<tr><td colspan="2">fitnesse.fixtures.ColumnFixtureTestFixture</td>
</tr>
<tr><td>integerInput</td>
<td>=integerOutput?</td>
</tr>
<tr><td>1</td>
<td>one <span class="fit_grey"> = 1</span></td>
</tr>
<tr><td>2</td>
<td>two <span class="fit_grey"> = 2</span></td>
</tr>
<tr><td>null</td>
<td>three <span class="fit_grey"> = null</span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">fitnesse.fixtures.ColumnFixtureTestFixture</td>
</tr>
<tr><td>integerInput=</td>
<td>integerOutput?</td>
</tr>
<tr><td>one <span class="fit_grey"> = 1</span></td>
<td class="pass">1</td>
</tr>
<tr><td>two <span class="fit_grey"> = 2</span></td>
<td class="pass">2</td>
</tr>
<tr><td>three <span class="fit_grey"> = null</span></td>
<td class="pass">null</td>
</tr>
</table>
<br/><pre>
public class ColumnFixtureTestFixture extends ColumnFixture
{
  public int input;
  public int output() {return input;}

  public Integer integerInput;
  public Integer integerOutput() { return integerInput; }

  public boolean exception() throws Exception {throw new Exception("I thowed up");}
}
</pre><br/><br/><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-8951502340202002254');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-8951502340202002254"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-8951502340202002254"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>5</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestBlankAndNullCells</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('8660276905673721170');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img8660276905673721170"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="8660276905673721170"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>If a cell contains <i>"blank"</i> or <i>"null"</i> then treat it as truly blank or truly null.</h3><br/>Lots of people have had trouble with blank cells.  In Fit, blank cells are automatically filled with the value of the variable or function, and no check is performed.  Unfortunately this means that there was no good test for truly null or truly blank fields.  So these keywords were added to allow users to enter them.<br/><br/><table border="1" cellspacing="0">
<tr><td colspan="6">fitnesse.fixtures.NullAndBlankFixture</td>
</tr>
<tr><td>nullString</td>
<td>blankString</td>
<td>nullString?</td>
<td>blankString?</td>
<td>isNull?</td>
<td>isBlank?</td>
</tr>
<tr><td>null</td>
<td>blank</td>
<td class="pass">null</td>
<td class="pass">blank</td>
<td class="pass">Y</td>
<td class="pass">Y</td>
</tr>
<tr><td>&nbsp; <span class="fit_grey">null</span></td>
<td>&nbsp; <span class="fit_grey">blank</span></td>
<td>&nbsp; <span class="fit_grey">null</span></td>
<td>&nbsp; <span class="fit_grey">blank</span></td>
<td class="pass">Y</td>
<td class="pass">Y</td>
</tr>
<tr><td>bob</td>
<td>micah</td>
<td>&nbsp; <span class="fit_grey">null</span></td>
<td>&nbsp; <span class="fit_grey">blank</span></td>
<td class="pass">N</td>
<td class="pass">N</td>
</tr>
</table>
<br/><pre>
public class NullAndBlankFixture extends ColumnFixture
{
  public String nullString;
  public String blankString;
  public String nullString() {return null;}
  public String blankString() {return "";}
  public boolean isNull() {return nullString == null;}
  public boolean isBlank() {return blankString.length() == 0;}
}
</pre><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('3308558240408222876');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img3308558240408222876"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="3308558240408222876"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>8</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestCannotResolveGracefullyNamedFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('1146840863597246448');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img1146840863597246448"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="1146840863597246448"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table refers to a graceful name resolving to a class that does not extend Fixture, then the following message should appear in that cell:</h3><pre>Class fixtureName is not a fixture.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">WouldBeFixture is a real class, but is not a Fixture</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Import|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|fitnesse.fixtures|</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>&nbsp;</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|Would Be|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NotFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NotFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Class fitnesse.fixtures.WouldBeFixture is not a fixture.</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

831
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NotFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NotFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NotFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NotFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NotFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NotFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NotFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NotFixturePage" class="page_title"&gt;NotFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

d5
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;Import&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;fitnesse.fixtures&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
b9

&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;Would Be&lt;hr/&gt; &lt;span class="fit_label"&gt;Class fitnesse.fixtures.WouldBeFixture is not a fixture.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

be
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('1833111460065982311');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img1833111460065982311"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="1833111460065982311"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestFixtureNotFound</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('4945176816872076494');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img4945176816872076494"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="4945176816872076494"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table does not refer to a class in the classpath, then the following message should appear in that cell:</h3><pre>Could not find fixture: fixtureName.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|No such fixture|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NoSuchFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NoSuchFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Could not find fixture: NoSuchFixture</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

84c
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NoSuchFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NoSuchFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NoSuchFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NoSuchFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NoSuchFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NoSuchFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NoSuchFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NoSuchFixturePage" class="page_title"&gt;NoSuchFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

113
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;No such fixture&lt;hr/&gt; &lt;span class="fit_label"&gt;Could not find fixture: NoSuchFixture.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

c4
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NoSuchFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NoSuchFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response requester</td>
</tr>
<tr><td>uri</td>
<td>contents?</td>
</tr>
<tr><td>ErrorLogs.NoSuchFixturePage</td>
<td>&nbsp; <span class="fit_grey"><pre>HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Cache-Control: max-age=0
Content-Length: 2422
Connection: close
Server: FitNesse-v20080908

&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;ErrorLogs.NoSuchFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;a href="/ErrorLogs"&gt;ErrorLogs&lt;/a&gt;.
				&lt;br/&gt;&lt;span class="page_title"&gt;NoSuchFixturePage&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;h3&gt;fit:fit.FitServer&lt;/h3&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Date: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;11:33:20 AM (CST) on Friday, November 7, 2008&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Command: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;java -cp ./classes:./fitnesse.jar fit.FitServer BobsMacbook.local 9123 1&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Exit code: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;1&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Time elapsed: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;0.272 seconds&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;div class="footer"&gt;
&lt;/div&gt;
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;
</pre></span></td>
</tr>
</table>
<br/><br/><br/><br/><br/><br/><br/><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-1066528352878370431');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-1066528352878370431"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-1066528352878370431"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestFixtureNotFoundAfterTackingOnFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-7199536045502944545');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-7199536045502944545"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-7199536045502944545"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table does not refer to a class in the classpath, then the following message should appear in that cell:</h3><pre>Could not find fixture: fixtureName.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!|NoSuch|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NoSuchFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NoSuchFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Could not find fixture: NoSuch</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

84c
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NoSuchFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NoSuchFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NoSuchFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NoSuchFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NoSuchFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NoSuchFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NoSuchFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NoSuchFixturePage" class="page_title"&gt;NoSuchFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

103
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;NoSuch&lt;hr/&gt; &lt;span class="fit_label"&gt;Could not find fixture: NoSuch.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

c4
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NoSuchFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NoSuchFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response requester</td>
</tr>
<tr><td>uri</td>
<td>contents?</td>
</tr>
<tr><td>ErrorLogs.NoSuchFixturePage</td>
<td>&nbsp; <span class="fit_grey"><pre>HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Cache-Control: max-age=0
Content-Length: 2422
Connection: close
Server: FitNesse-v20080908

&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;ErrorLogs.NoSuchFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="ErrorLogs.NoSuchFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;a href="/ErrorLogs"&gt;ErrorLogs&lt;/a&gt;.
				&lt;br/&gt;&lt;span class="page_title"&gt;NoSuchFixturePage&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;h3&gt;fit:fit.FitServer&lt;/h3&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Date: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;11:33:20 AM (CST) on Friday, November 7, 2008&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Command: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;java -cp ./classes:./fitnesse.jar fit.FitServer BobsMacbook.local 9123 1&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Exit code: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;1&lt;/td&gt;
&lt;/tr&gt;
&lt;tr&gt;&lt;td&gt;&lt;b&gt;Time elapsed: &lt;/b&gt;&lt;/td&gt;
&lt;td&gt;0.266 seconds&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;
&lt;br/&gt;&lt;div class="footer"&gt;
&lt;/div&gt;
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;
</pre></span></td>
</tr>
</table>
<br/><br/><br/><br/><br/><br/><br/><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('3022885196241627770');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img3022885196241627770"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="3022885196241627770"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestNoDefaultConstructor</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-5976018093317471691');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-5976018093317471691"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-5976018093317471691"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table refers to a fixture class without a default (no argument) constructor, then the following message should appear in that cell:</h3><pre>Class fixtureName has no default constructor.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">NoDefaultConstructorFixture is a real class, but is not a Fixture</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|!-fitnesse.fixtures.NoDefaultConstructorFixture-!|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NotFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NotFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Class fitnesse.fixtures.NoDefaultConstructorFixture has no default constructor.</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

831
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NotFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NotFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NotFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NotFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NotFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NotFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NotFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NotFixturePage" class="page_title"&gt;NotFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

15a
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;fitnesse.fixtures.NoDefaultConstructorFixture&lt;hr/&gt; &lt;span class="fit_label"&gt;Class fitnesse.fixtures.NoDefaultConstructorFixture has no default constructor.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

be
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-2418948039055896102');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-2418948039055896102"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-2418948039055896102"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestNotFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('5642713307985887998');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img5642713307985887998"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="5642713307985887998"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table refers to a class that does not extend Fixture, then the following message should appear in that cell:</h3><pre>Class fixtureName is not a fixture.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">WouldBeFixture is a real class, but is not a Fixture</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|!-fitnesse.fixtures.WouldBeFixture-!|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NotFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NotFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Class fitnesse.fixtures.WouldBeFixture is not a fixture.</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

831
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NotFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NotFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NotFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NotFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NotFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NotFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NotFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NotFixturePage" class="page_title"&gt;NotFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

136
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;fitnesse.fixtures.WouldBeFixture&lt;hr/&gt; &lt;span class="fit_label"&gt;Class fitnesse.fixtures.WouldBeFixture is not a fixture.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

be
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('4805836189911558446');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img4805836189911558446"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="4805836189911558446"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestParsingOfObjects</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-2429720656416557135');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-2429720656416557135"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-2429720656416557135"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
Table cells contain strings.  Fixtures deal with objects.  In order to convert the strings into the objects FIT needs to know how to parse the strings.  One way we accomplish this is to allow the objects to have the following method:<pre>Object parse(String s);</pre>The following table shows the result of adding two vectors.  Each vector is represented by an ordered pair which represents it's X and Y dimensions.  The class CartesianVector<a title="create page" href="FitNesse.SuiteAcceptanceTests.SuiteFixtureTests.SuiteGeneralFixtureSpec.CartesianVector?edit&amp;nonExistent=true">[?]</a> is used to parse, display, and sum the vectors.<br/><br/><table border="1" cellspacing="0">
<tr><td colspan="3">fitnesse.testutil.VectorSum</td>
</tr>
<tr><td>v1</td>
<td>v2</td>
<td>sum?</td>
</tr>
<tr><td>(0,0)</td>
<td>(0,1)</td>
<td class="pass">(0,1)</td>
</tr>
<tr><td>(0,1)</td>
<td>(0,1)</td>
<td class="pass">(0,2)</td>
</tr>
<tr><td>(1,1)</td>
<td>(1,1)</td>
<td class="pass">(2,2)</td>
</tr>
</table>
<br/>It is not always possible to add a parse method on the Object returned by the fixture.<br/><br/>For Ex. if your fixture returns java.awt.Point class which does not have a <pre>Object parse(String s);</pre> method, this approach won't work.<br/><br/>Following approach can be used to delegate the parse method to a different class (Parse Delegate class). The parse delegate class has the <pre>Object parse(String s);</pre> method which returns the Object we are interested in.<br/><br/>The following table shows the result of adding two points.  Each point is represented by an ordered pair which represents it's X and Y dimensions.<br/><br/><table border="1" cellspacing="0">
<tr><td colspan="3">fitnesse.testutil.ObjectTranslatePoint</td>
</tr>
<tr><td>p1</td>
<td>p2</td>
<td>sum?</td>
</tr>
<tr><td>(0,0)</td>
<td>(0,1)</td>
<td class="pass">(0,1)</td>
</tr>
<tr><td>(0,1)</td>
<td>(0,1)</td>
<td class="pass">(0,2)</td>
</tr>
<tr><td>(1,1)</td>
<td>(1,1)</td>
<td class="pass">(2,2)</td>
</tr>
</table>
<br/>In the ObjectTranslatePoint<a title="create page" href="FitNesse.SuiteAcceptanceTests.SuiteFixtureTests.SuiteGeneralFixtureSpec.ObjectTranslatePoint?edit&amp;nonExistent=true">[?]</a> fixture, we have a static block which registers the parse delegate object for a give Class type.<br/><br/>Ex:<br/><pre>static
{
        TypeAdapter.registerParseDelegate(java.awt.Point.class, new ObjectDelegatePointParser());
}
</pre>Please note that we are passing a Object of the Parse Delegate class. It is also possible to pass a class instead of the object. Only difference being the parse delegate class should have a <pre>public static Object parse(String s);</pre> method.<br/><br/><table border="1" cellspacing="0">
<tr><td colspan="3">fitnesse.testutil.ClassTranslatePoint</td>
</tr>
<tr><td>p1</td>
<td>p2</td>
<td>sum?</td>
</tr>
<tr><td>(0,0)</td>
<td>(0,1)</td>
<td class="pass">(0,1)</td>
</tr>
<tr><td>(0,1)</td>
<td>(0,1)</td>
<td class="pass">(0,2)</td>
</tr>
<tr><td>(1,1)</td>
<td>(1,1)</td>
<td class="pass">(2,2)</td>
</tr>
</table>
<br/>In the ClassTranslatePoint<a title="create page" href="FitNesse.SuiteAcceptanceTests.SuiteFixtureTests.SuiteGeneralFixtureSpec.ClassTranslatePoint?edit&amp;nonExistent=true">[?]</a> fixture, we have a static block which registers the parse delegate class for a give Class type.<br/><br/>Ex:<br/><pre>static
{
        TypeAdapter.registerParseDelegate(java.awt.Point.class, ClassDelegatePointParser.class);
}
</pre><br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('8148195318829884260');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img8148195318829884260"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="8148195318829884260"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>9</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteGeneralFixtureSpec.TestTackOnFixtureNotFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('4676136695637802627');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img4676136695637802627"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="4676136695637802627"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
<h3>When testing a table, if the first cell of the table refers to a class (after adding 'Fixture' to the end of the name) that does not extend Fixture, then the following message should appear in that cell:</h3><pre>Class fixtureName is not a fixture.</pre><br/><br/><ul>
	<li>Here is a fitnesse page that should generate the error</li>
</ul>
<span class="note">The !path must point to fitnesse.jar</span><br/><span class="note">WouldBeFixture is a real class, but is not a Fixture</span><br/><table border="1" cellspacing="0">
<tr><td colspan="3">Action fixture</td>
</tr>
<tr><td>start</td>
<td colspan="2">Page builder</td>
</tr>
<tr><td>enter</td>
<td>attributes</td>
<td>Test=true</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./classes</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>!path ./fitnesse.jar</td>
</tr>
<tr><td>enter</td>
<td>line</td>
<td>|!-fitnesse.fixtures.WouldBe-!|</td>
</tr>
<tr><td>enter</td>
<td>page</td>
<td>NotFixturePage</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td colspan="2">Response Requester</td>
</tr>
<tr><td>uri</td>
<td>status?</td>
</tr>
<tr><td>NotFixturePage?test</td>
<td class="pass">200</td>
</tr>
</table>
<br/><ul>
	<li>The error message should show up in the response</li>
</ul>
<br/><table border="1" cellspacing="0">
<tr><td colspan="4">Response examiner</td>
</tr>
<tr><td>type</td>
<td>pattern</td>
<td>matches?</td>
<td>contents?</td>
</tr>
<tr><td>contents</td>
<td>Class fitnesse.fixtures.WouldBeFixture is not a fixture.</td>
<td class="pass">true</td>
<td>&nbsp; <span class="fit_grey">HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Connection: close
Server: FitNesse-v20080908
Transfer-Encoding: chunked

831
&lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
&lt;html&gt;
	&lt;head&gt;
		&lt;title&gt;Test Results: NotFixturePage&lt;/title&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse.css" media="screen"/&gt;
		&lt;link rel="stylesheet" type="text/css" href="/files/css/fitnesse_print.css" media="print"/&gt;
		&lt;script src="/files/javascript/fitnesse.js" type="text/javascript"&gt;&lt;/script&gt;
	&lt;/head&gt;
	&lt;body&gt;
		&lt;div class="sidebar"&gt;
			&lt;div class="art_niche" onclick="document.location='/'"&gt;&lt;/div&gt;
			&lt;div class="actions"&gt;
				&lt;!--Test button--&gt;
				&lt;a href="NotFixturePage?test" accesskey="t"&gt;Test&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Edit button--&gt;
				&lt;a href="NotFixturePage?edit" accesskey="e"&gt;Edit&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Properties button--&gt;
				&lt;a href="NotFixturePage?properties" accesskey="p"&gt;Properties&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Refactor button--&gt;
				&lt;a href="NotFixturePage?refactor" accesskey="r"&gt;Refactor&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Where Used button--&gt;
				&lt;a href="NotFixturePage?whereUsed" accesskey="w"&gt;Where Used&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Search button--&gt;
				&lt;a href="?searchForm" accesskey="s"&gt;Search&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Files button--&gt;
				&lt;a href="/files" accesskey="f"&gt;Files&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Versions button--&gt;
				&lt;a href="NotFixturePage?versions" accesskey="v"&gt;Versions&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--Recent Changes button--&gt;
				&lt;a href="/RecentChanges" accesskey=""&gt;Recent Changes&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
				&lt;!--User Guide button--&gt;
				&lt;a href=".FitNesse.UserGuide" accesskey=""&gt;User Guide&lt;/a&gt;
				&lt;div class="nav_break"&gt;&amp;nbsp;&lt;/div&gt;
			&lt;/div&gt;
		&lt;/div&gt;
		&lt;div class="mainbar"&gt;
			&lt;div class="header"&gt;
				&lt;br/&gt;&lt;a href="/NotFixturePage" class="page_title"&gt;NotFixturePage&lt;/a&gt;
&lt;br/&gt;				&lt;span class="page_type"&gt;Test Results&lt;/span&gt;
			&lt;/div&gt;
			&lt;div class="main"&gt;&lt;div id="test-summary"&gt;Running Tests ...&lt;/div&gt;

12f
&lt;span class="meta"&gt;classpath: ./classes&lt;/span&gt;&lt;br/&gt;&lt;span class="meta"&gt;classpath: ./fitnesse.jar&lt;/span&gt;&lt;br/&gt;&lt;table border="1" cellspacing="0"&gt;
&lt;tr&gt;&lt;td class="error"&gt;fitnesse.fixtures.WouldBe&lt;hr/&gt; &lt;span class="fit_label"&gt;Class fitnesse.fixtures.WouldBeFixture is not a fixture.&lt;/span&gt;&lt;/td&gt;
&lt;/tr&gt;
&lt;/table&gt;

cb
&lt;script&gt;document.getElementById("test-summary").innerHTML = "&lt;strong&gt;Assertions:&lt;/strong&gt; 0 right, 0 wrong, 0 ignored, 1 exceptions";document.getElementById("test-summary").className = "error";&lt;/script&gt;

be
&lt;div id="execution-status"&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;&lt;img src="/files/images/executionStatus/ok.gif"/&gt;
&lt;/a&gt;
&lt;br/&gt;
	&lt;a href="ErrorLogs.NotFixturePage"&gt;Tests Executed OK&lt;/a&gt;
&lt;/div&gt;

21
&lt;/div&gt;
		&lt;/div&gt;
	&lt;/body&gt;
&lt;/html&gt;

0
Exit-Code: 1

</span></td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-78396559528540840');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-78396559528540840"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-78396559528540840"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>2</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<result>
		<relativePageName>SuiteRowFixtureSpec.TestBasicRowFixture</relativePageName>
		<content><![CDATA[<br/><div class="setup">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-5302141934063354336');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-5302141934063354336"/>
	</a>
&nbsp;<span class="meta">Set Up: <a href="FitNesse.SuiteAcceptanceTests.SetUp">.FitNesse.SuiteAcceptanceTests.SetUp</a> <a href="FitNesse.SuiteAcceptanceTests.SetUp?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-5302141934063354336"><table border="1" cellspacing="0">
<tr><td>Import</td>
</tr>
<tr><td>fitnesse.fixtures</td>
</tr>
</table>
<br/><table border="1" cellspacing="0">
<tr><td>SetUp</td>
</tr>
</table>
</div>
</div>
A simple list of prime numbers.  This fixture should pass.<br/><br/><table border="1" cellspacing="0">
<tr><td>fitnesse.fixtures.PrimeNumberRowFixture</td>
</tr>
<tr><td>prime</td>
</tr>
<tr><td class="pass">2</td>
</tr>
<tr><td class="pass">3</td>
</tr>
<tr><td class="pass">5</td>
</tr>
<tr><td class="pass">7</td>
</tr>
<tr><td class="pass">11</td>
</tr>
</table>
<br/><div class="teardown">
	<div style="float: right;" class="meta"><a href="javascript:expandAll();">Expand All</a> | <a href="javascript:collapseAll();">Collapse All</a></div>
	<a href="javascript:toggleCollapsable('-5074515160426296402');">
		<img src="/files/images/collapsableClosed.gif" class="left" id="img-5074515160426296402"/>
	</a>
&nbsp;<span class="meta">Tear Down: <a href="FitNesse.SuiteAcceptanceTests.TearDown">.FitNesse.SuiteAcceptanceTests.TearDown</a> <a href="FitNesse.SuiteAcceptanceTests.TearDown?edit&amp;redirectToReferer=true&amp;redirectAction=">(edit)</a></span>
	<div class="hidden" id="-5074515160426296402"><hr/>
<br/><table border="1" cellspacing="0">
<tr><td>tear down</td>
</tr>
</table>
<br/></div>
</div>

]]></content>
		<counts>
			<right>5</right>
			<wrong>0</wrong>
			<ignores>0</ignores>
			<exceptions>0</exceptions>
		</counts>
	</result>

	<finalCounts>
		<right>44</right>
		<wrong>0</wrong>
		<ignores>0</ignores>
		<exceptions>0</exceptions>
	</finalCounts>
</testResults>
